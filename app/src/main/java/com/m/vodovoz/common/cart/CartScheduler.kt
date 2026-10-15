package com.m.vodovoz.common.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

internal class CartScheduler(
    private val cart: Cart,
    private val debounceMillis: Long,
    private val nowMillis: () -> Long,
) {
    private data class Pending(
        val commandId: UUID,
        val requestKind: RequestKind,
    )

    data class Wakeup(val version: Long, val atMillis: Long?)

    internal sealed interface Ready {
        data class Clear(val commandId: UUID, val revisions: Map<Long, Long>) : Ready
        data class Items(
            val ids: List<Long>,
            val requestKind: RequestKind,
            val commandsById: Map<Long, UUID>,
        ) : Ready {
            val commandIds: Set<UUID> get() = commandsById.values.toSet()
        }

        data class Batch(val noops: Map<Long, UUID>, val requests: List<Items>) : Ready
    }

    private val pending = mutableMapOf<Long, Pending>()
    private var pendingClear: Ready.Clear? = null
    private var readyAtMillis: Long? = null
    private val _wakeups = MutableStateFlow(Wakeup(0L, null))
    val wakeups: StateFlow<Wakeup> = _wakeups.asStateFlow()

    fun enqueue(ids: Set<Long>, mode: DispatchMode, kind: RequestKind, commandId: UUID): Map<Long, UUID> {
        val pendingBottleIds = pending.filterValues { it.requestKind == RequestKind.REPLACE_BOTTLES }.keys
        val replaced = ids.mapNotNull { id ->
            // Позднее изменение одной бутыли остаётся частью ожидающей полной замены группы.
            val effectiveKind = if (id in pendingBottleIds) RequestKind.REPLACE_BOTTLES else kind
            pending.put(id, Pending(commandId, effectiveKind))?.commandId?.let { id to it }
        }.toMap()
        readyAtMillis = nowMillis() + if (mode == DispatchMode.IMMEDIATE) 0L else debounceMillis
        signal()
        return replaced
    }

    /** Очистка — барьер: она отменяет ожидающие действия, а новые ставятся после неё. */
    fun enqueueClear(commandId: UUID, revisions: Map<Long, Long>): Map<Long, UUID> {
        val replaced = pending.mapValues { it.value.commandId }
        pending.clear()
        readyAtMillis = null
        pendingClear = Ready.Clear(commandId, revisions)
        signal()
        return replaced
    }

    fun contains(id: Long): Boolean = id in pending || pendingClear != null
    fun hasClear(): Boolean = pendingClear != null
    fun hasPending(): Boolean = pending.isNotEmpty() || pendingClear != null

    fun takeClear(): Ready.Clear? = pendingClear.also { pendingClear = null }

    /** За один тик забирает все свободные ID. Занятые остаются до ответа своего запроса. */
    fun takeReady(busyIds: Set<Long>): Ready.Batch? {
        if (pendingClear != null || (readyAtMillis ?: return null) > nowMillis()) return null
        val bottleIds = pending.filterValues { it.requestKind == RequestKind.REPLACE_BOTTLES }.keys
        val bottlesBusy = bottleIds.any { it in busyIds }
        val available = pending.keys.filterNot {
            it in busyIds || (bottlesBusy && it in bottleIds)
        }.sorted()
        if (available.isEmpty()) return null

        val chosen = available.mapNotNull { id -> pending.remove(id)?.let { id to it } }.toMap()
        if (chosen.isEmpty()) {
            if (pending.isEmpty()) readyAtMillis = null
            return null
        }
        if (pending.isEmpty()) readyAtMillis = null

        // Сервер заменяет всю группу бутылей, поэтому неизменившиеся ID тоже нужны в запросе.
        val noops = chosen.filter { (id, item) ->
            !cart.needsSync(id) && item.requestKind != RequestKind.REPLACE_BOTTLES
        }
            .mapValues { it.value.commandId }
        val changed = chosen.keys - noops.keys
        val requests = mutableListOf<Ready.Items>()

        fun addRequest(ids: List<Long>, kind: RequestKind) {
            if (ids.isEmpty()) return
            val commandsById = ids.mapNotNull { id -> chosen[id]?.let { id to it.commandId } }.toMap()
            if (commandsById.isNotEmpty()) {
                requests += Ready.Items(commandsById.keys.toList(), kind, commandsById)
            }
        }

        val addOrChange = changed.filter { chosen[it]?.requestKind == RequestKind.ADD_OR_CHANGE }
        val newProducts = addOrChange.filter {
            cart.item(it).let { item -> item.confirmedQuantity == 0 && item.quantity > 0 }
        }
        addRequest(newProducts, RequestKind.ADD_OR_CHANGE)
        addOrChange.filterNot { it in newProducts }.forEach {
            addRequest(listOf(it), RequestKind.ADD_OR_CHANGE)
        }

        for (kind in listOf(RequestKind.ADD_MULTIPLE, RequestKind.REPLACE_BOTTLES)) {
            addRequest(changed.filter { chosen[it]?.requestKind == kind }, kind)
        }
        return Ready.Batch(noops, requests)
    }

    /** После сетевого ответа возобновляет ожидание прежнего срока, не сбрасывая debounce. */
    fun wakeAfterResult() = signal()

    fun reset() {
        pending.clear()
        pendingClear = null
        readyAtMillis = null
        signal()
    }

    private fun signal() {
        _wakeups.value = Wakeup(
            _wakeups.value.version + 1,
            if (pendingClear != null) nowMillis() else readyAtMillis,
        )
    }
}
