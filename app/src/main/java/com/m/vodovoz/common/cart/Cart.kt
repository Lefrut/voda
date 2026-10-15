package com.m.vodovoz.common.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Cart {
    private val _itemsState = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    val itemsState: StateFlow<Map<Long, CartItem>> = _itemsState.asStateFlow()

    internal fun loadFromServer(quantities: Map<Long, Int>) {
        _itemsState.value = quantities
            .filterValues { it > 0 }
            .mapValues { (id, quantity) ->
                CartItem(id = id, quantity = quantity, confirmedQuantity = quantity)
            }
    }

    internal fun ids(): Set<Long> = _itemsState.value.keys
    internal fun item(id: Long): CartItem = _itemsState.value[id] ?: CartItem(id, 0, 0)
    internal fun needsSync(id: Long): Boolean = item(id).let { it.quantity != it.confirmedQuantity }

    /** Меняем желаемые количества сразу, одним снимком для подписчиков. */
    internal fun setQuantities(targets: Map<Long, Int>) {
        val next = _itemsState.value.toMutableMap()
        targets.forEach { (id, quantity) ->
            val current = next[id] ?: CartItem(id, 0, 0)
            next[id] = current.copy(
                quantity = quantity.coerceAtLeast(0),
                status = if (current.status == SyncStatus.SENDING) SyncStatus.SENDING else SyncStatus.QUEUED,
                revision = current.revision + 1,
            )
        }
        _itemsState.value = next.toMap()
    }

    /** Версии на момент Clear: по ним отличим позднее действие от старого запроса. */
    internal fun clear(): Map<Long, Long> {
        setQuantities(ids().associateWith { 0 })
        return ids().associateWith { item(it).revision }
    }

    internal fun settleNoops(ids: Collection<Long>) {
        val next = _itemsState.value.toMutableMap()
        ids.forEach { id ->
            val current = next[id] ?: return@forEach
            if (current.quantity == current.confirmedQuantity) {
                next[id] = current.copy(status = SyncStatus.IDLE)
            }
        }
        publish(next)
    }

    internal fun markSending(ids: Collection<Long>, revisions: Map<Long, Long>) {
        val next = _itemsState.value.toMutableMap()
        ids.forEach { id ->
            next[id]?.let { item ->
                if (item.revision == revisions[id]) {
                    next[id] = item.copy(status = SyncStatus.SENDING)
                }
            }
        }
        publish(next)
    }

    /** Ответ обновляет confirmed, сохраняя более свежую команду пользователя. */
    internal fun applyResult(
        targets: Map<Long, Int>,
        sentRevisions: Map<Long, Long>,
        result: Result<Unit>,
    ) {
        val next = _itemsState.value.toMutableMap()
        targets.forEach { (id, sentQuantity) ->
            val current = next[id] ?: return@forEach
            next[id] = when {
                result.isSuccess -> current.copy(
                    confirmedQuantity = sentQuantity,
                    status = if (current.quantity == sentQuantity) SyncStatus.IDLE else SyncStatus.QUEUED,
                )

                current.revision == sentRevisions[id] -> current.copy(
                    quantity = current.confirmedQuantity,
                    status = SyncStatus.FAILED,
                )

                else -> current.copy(status = SyncStatus.QUEUED)
            }
        }
        publish(next)
    }

    private fun publish(items: Map<Long, CartItem>) {
        _itemsState.value = items.filterValues { it.quantity > 0 || it.status != SyncStatus.IDLE }
    }
}
