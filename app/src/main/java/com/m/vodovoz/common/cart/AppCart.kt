package com.m.vodovoz.common.cart

import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

enum class DispatchMode { DEBOUNCED, IMMEDIATE }

/** REPLACE_BOTTLES рассчитан на изменение перечисленных ID; полная замена группы требует отдельного ключа конфликта. */
enum class RequestKind { AUTO, ADD_MULTIPLE, REPLACE_BOTTLES }
enum class SyncStatus { IDLE, QUEUED, SENDING, FAILED }

/** quantity — то, что видит пользователь; confirmedQuantity — последний успешный ответ сервера. */
data class CartItem(
    val id: Long,
    val quantity: Int,
    val confirmedQuantity: Int,
    val status: SyncStatus = SyncStatus.IDLE,
    val revision: Long = 0,
) {
    val isLoading: Boolean get() = status == SyncStatus.QUEUED || status == SyncStatus.SENDING
}

/**
 * Корзина — один источник состояния. StateFlow хранит неизменяемые снимки Map.
 * Методы изменения вызываются только из последовательного цикла CartManager.
 */
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
            require(quantity >= 0)
            val current = next[id] ?: CartItem(id, 0, 0)
            next[id] = current.copy(
                quantity = quantity,
                status = SyncStatus.QUEUED,
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

    internal fun markSending(ids: Collection<Long>) {
        val next = _itemsState.value.toMutableMap()
        ids.forEach { id ->
            next[id]?.let { next[id] = it.copy(status = SyncStatus.SENDING) }
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

/** Отличия endpoint остаются на границе с API, а не внутри Cart. */
sealed interface CartRequest {
    sealed interface SingleItem : CartRequest {
        val id: Long
        val quantity: Int
    }

    sealed interface MultiItem : CartRequest {
        val items: Map<Long, Int>
    }

    data class AddProduct(override val id: Long, override val quantity: Int) : SingleItem
    data class UpdateProduct(override val id: Long, override val quantity: Int) : SingleItem
    data class AddProducts(override val items: Map<Long, Int>) : MultiItem
    data class ReplaceBottles(override val items: Map<Long, Int>) : MultiItem
    data object Clear : CartRequest
}

interface CartRepository {
    suspend fun execute(request: CartRequest): Result<Unit>
}

sealed interface CartResult {
    val requestId: UUID
    val commandIds: Set<UUID>
    val request: CartRequest

    data class Succeeded(
        override val requestId: UUID,
        override val commandIds: Set<UUID>,
        override val request: CartRequest,
    ) : CartResult

    data class Failed(
        override val requestId: UUID,
        override val commandIds: Set<UUID>,
        override val request: CartRequest,
        val error: Throwable,
    ) : CartResult
}

/**
 * Очередь и debounce живут отдельно от CartManager. Все методы, кроме срабатывания
 * таймера, вызываются из одного цикла обработки сообщений CartManager.
 * Последняя команда для ID заменяет прежнюю, включая время отправки и endpoint.
 */
internal class CartScheduler(
    private val cart: Cart,
    private val scope: CoroutineScope,
    private val debounceMillis: Long,
    private val nowMillis: () -> Long,
    private val wakeUp: suspend () -> Unit,
) {
    private data class Pending(
        val commandId: UUID,
        val deadline: Long,
        val mode: DispatchMode,
        val requestKind: RequestKind,
    )

    internal sealed interface Ready {
        data class Clear(val commandId: UUID, val revisions: Map<Long, Long>) : Ready
        data class Noop(val ids: List<Long>) : Ready
        data class Items(
            val ids: List<Long>,
            val requestKind: RequestKind,
            val commandIds: Set<UUID>,
        ) : Ready
    }

    private val pending = mutableMapOf<Long, Pending>()
    private var pendingClear: Ready.Clear? = null
    private var timer: Job? = null

    fun enqueue(ids: Set<Long>, mode: DispatchMode, kind: RequestKind, commandId: UUID) {
        val deadline = nowMillis() + if (mode == DispatchMode.IMMEDIATE) 0L else debounceMillis
        ids.forEach { pending[it] = Pending(commandId, deadline, mode, kind) }
    }

    /** Очистка — барьер: она отменяет ожидающие действия, а новые ставятся после неё. */
    fun enqueueClear(commandId: UUID, revisions: Map<Long, Long>) {
        pending.clear()
        pendingClear = Ready.Clear(commandId, revisions)
    }

    fun contains(id: Long): Boolean = id in pending || pendingClear != null

    /** Выбирает один пакет; сетевой запрос создаёт и запускает CartManager. */
    fun takeReady(): Ready? {
        pendingClear?.let { clear ->
            pendingClear = null
            return clear
        }

        val ready = pending.entries.filter {
            it.value.deadline <= nowMillis()
        }.sortedWith(
            compareBy<Map.Entry<Long, Pending>> {
                if (it.value.mode == DispatchMode.IMMEDIATE) 0 else 1
            }
                .thenBy { it.value.deadline }
                .thenBy { it.key }
        )

        if (ready.isEmpty()) return null

        val noops = ready.filter {
            !cart.needsSync(it.key) && it.value.requestKind != RequestKind.REPLACE_BOTTLES
        }
        if (noops.isNotEmpty()) {
            val ids = noops.map { it.key }
            ids.forEach { pending.remove(it) }
            return Ready.Noop(ids)
        }

        val changed = ready.filter { cart.needsSync(it.key) }
        if (changed.isEmpty()) {
            val ids = ready.map { it.key }
            ids.forEach { pending.remove(it) }
            return Ready.Noop(ids)
        }

        val first = changed.first()
        val kind = first.value.requestKind
        val ids = when (kind) {
            RequestKind.AUTO -> {
                val item = cart.item(first.key)
                if (item.confirmedQuantity == 0 && item.quantity > 0) {
                    changed.filter {
                        it.value.requestKind == RequestKind.AUTO &&
                                cart.item(it.key).confirmedQuantity == 0 &&
                                cart.item(it.key).quantity > 0
                    }.map { it.key }
                } else {
                    listOf(first.key)
                }
            }

            RequestKind.ADD_MULTIPLE -> changed.filter { it.value.requestKind == kind }
                .map { it.key }

            RequestKind.REPLACE_BOTTLES -> ready.filter { it.value.requestKind == kind }
                .map { it.key }
        }
        val commandIds = ids.map { checkNotNull(pending[it]).commandId }.toSet()
        ids.forEach { pending.remove(it) }
        return Ready.Items(ids, kind, commandIds)
    }

    /** Один таймер на ближайший запрос; после ответа он будет поставлен заново. */
    fun armNextDeadline() {
        pause()
        val deadline = pending.values.minOfOrNull { it.deadline } ?: return
        timer = scope.launch {
            delay((deadline - nowMillis()).coerceAtLeast(0L))
            wakeUp()
        }
    }

    fun pause() {
        timer?.cancel()
        timer = null
    }

    fun reset() {
        pause()
        pending.clear()
        pendingClear = null
    }
}

/**
 * Последовательно обрабатывает действия и ответы через Channel. Cart хранит
 * снимок состояния; CartScheduler выбирает момент и состав запроса.
 */
class AppCartManager(
    parentScope: CoroutineScope,
    private val repository: CartRepository,
    private val onResult: suspend (CartResult) -> Unit = {},
    debounceMillis: Long = 300L,
    nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
) {
    init {
        require(debounceMillis >= 0)
    }

    /** Зафиксированный снимок уже отправленного запроса: нужен при позднем ответе. */
    private data class SentRequest(
        val id: UUID,
        val generation: Long,
        val request: CartRequest,
        val targets: Map<Long, Int>,
        val revisions: Map<Long, Long>,
        val commandIds: Set<UUID>,
        val requestKind: RequestKind = RequestKind.AUTO,
    )

    private sealed interface Command {
        data class Set(val targets: Map<Long, Int>, val mode: DispatchMode, val kind: RequestKind) :
            Command

        data class Add(val deltas: Map<Long, Int>, val mode: DispatchMode, val kind: RequestKind) :
            Command

        data object Clear : Command
    }

    /** Внутренний вход в последовательный цикл; CartResult — внешний результат запроса. */
    private sealed interface InboxMessage {
        data class LoadCart(val items: Map<Long, Int>, val done: CompletableDeferred<Unit>) :
            InboxMessage

        data class UserAction(val id: UUID, val command: Command) : InboxMessage
        data class NetworkCompleted(val sent: SentRequest, val result: Result<Unit>) : InboxMessage
        data object TimerExpired : InboxMessage
    }

    private val scope =
        CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private val inbox = Channel<InboxMessage>(Channel.UNLIMITED)
    private val cart = Cart()
    val itemsState: StateFlow<Map<Long, CartItem>> get() = cart.itemsState

    private val _results = MutableSharedFlow<CartResult>(extraBufferCapacity = 64)
    val results: SharedFlow<CartResult> = _results.asSharedFlow()

    private val scheduler = CartScheduler(
        cart = cart,
        scope = scope,
        debounceMillis = debounceMillis,
        nowMillis = nowMillis,
        wakeUp = {
            inbox.send(InboxMessage.TimerExpired)
        }
    )

    private var inFlight: SentRequest? = null
    private var requestJob: Job? = null
    private var generation = 0L
    private var loaded = false
    private val waitingForLoad = ArrayDeque<InboxMessage.UserAction>()

    init {
        scope.coroutineContext[Job]?.invokeOnCompletion { inbox.close() }
        observeInbox()
    }


    private fun observeInbox() = scope.launch {
        for (message in inbox) {
            when (message) {
                is InboxMessage.LoadCart -> load(message)
                is InboxMessage.UserAction -> if (loaded) accept(message) else waitingForLoad.addLast(
                    message
                )

                is InboxMessage.NetworkCompleted -> finish(message)
                InboxMessage.TimerExpired -> Unit
            }
            if (loaded) dispatchReady()
        }
    }


    /**
     * Вызывай при старте приложения или смене пользователя после получения корзины.
     * Повторная загрузка сбрасывает очередь и игнорирует ответы прошлого поколения.
     */
    suspend fun loadCart(quantities: Map<Long, Int>) {
        require(quantities.values.all { it >= 0 })
        val done = CompletableDeferred<Unit>()
        inbox.send(InboxMessage.LoadCart(quantities.toMap(), done))
        done.await()
    }

    /** Абсолютное количество товара. Для бутылки передай requestKind = REPLACE_BOTTLES. */
    fun setQuantity(
        id: Long,
        quantity: Int,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.AUTO,
    ): UUID = setQuantities(mapOf(id to quantity), mode, requestKind)

    /** Несколько товаров идут тем же путём; kind определяет нужный endpoint. */
    fun setQuantities(
        targets: Map<Long, Int>,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.AUTO,
    ): UUID {
        require(targets.isNotEmpty() && targets.values.all { it >= 0 })
        return submit(Command.Set(targets.toMap(), mode, requestKind))
    }

    fun adjustQuantity(
        id: Long,
        delta: Int,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.AUTO,
    ): UUID = submit(Command.Add(mapOf(id to delta), mode, requestKind))

    fun incrementProducts(
        deltas: Map<Long, Int>,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.ADD_MULTIPLE,
    ): UUID {
        require(deltas.isNotEmpty() && deltas.values.all { it > 0 })
        return submit(Command.Add(deltas.toMap(), mode, requestKind))
    }

    fun clear(): UUID = submit(Command.Clear)

    fun close() {
        inbox.close()
        scope.cancel()
    }

    private fun submit(command: Command): UUID {
        val id = UUID.randomUUID()
        check(
            scope.coroutineContext[Job]?.isActive == true && inbox.trySend(
                InboxMessage.UserAction(
                    id,
                    command
                )
            ).isSuccess
        )
        return id
    }

    private fun load(message: InboxMessage.LoadCart) {
        generation++
        requestJob?.cancel()
        requestJob = null
        inFlight = null
        scheduler.reset()
        cart.loadFromServer(message.items)
        loaded = true
        while (waitingForLoad.isNotEmpty()) accept(waitingForLoad.removeFirst())
        message.done.complete(Unit)
    }

    private fun accept(message: InboxMessage.UserAction) {
        when (val command = message.command) {
            is Command.Set -> {
                cart.setQuantities(command.targets)
                scheduler.enqueue(command.targets.keys, command.mode, command.kind, message.id)
            }

            is Command.Add -> {
                val targets = command.deltas.mapValues { (id, delta) ->
                    (cart.item(id).quantity.toLong() + delta)
                        .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                }
                cart.setQuantities(targets)
                scheduler.enqueue(targets.keys, command.mode, command.kind, message.id)
            }

            Command.Clear -> {
                scheduler.enqueueClear(message.id, cart.clear())
            }
        }
    }

    private fun finish(message: InboxMessage.NetworkCompleted) {
        val sent = message.sent
        if (sent.generation != generation || inFlight?.id != sent.id) return
        inFlight = null
        requestJob = null
        cart.applyResult(sent.targets, sent.revisions, message.result)
        sent.targets.keys.forEach { id ->
            if (cart.needsSync(id) && !scheduler.contains(id)) {
                scheduler.enqueue(
                    setOf(id),
                    DispatchMode.IMMEDIATE,
                    sent.requestKind,
                    UUID.randomUUID()
                )
            }
        }
        val outcome = if (message.result.isSuccess) {
            CartResult.Succeeded(sent.id, sent.commandIds, sent.request)
        } else {
            CartResult.Failed(
                sent.id, sent.commandIds, sent.request,
                checkNotNull(message.result.exceptionOrNull()),
            )
        }
        _results.tryEmit(outcome)
        scope.launch { onResult(outcome) }
    }

    private fun dispatchReady() {
        scheduler.pause()
        if (inFlight != null) return

        while (true) {
            when (val ready = scheduler.takeReady()) {
                is CartScheduler.Ready.Clear -> {
                    val targets = cart.ids().associateWith { 0 }
                    start(
                        SentRequest(
                            id = UUID.randomUUID(), generation = generation,
                            request = CartRequest.Clear, targets = targets,
                            revisions = ready.revisions, commandIds = setOf(ready.commandId),
                        )
                    )
                    return
                }

                is CartScheduler.Ready.Noop -> cart.settleNoops(ready.ids)
                is CartScheduler.Ready.Items -> {
                    val targets = ready.ids.associateWith { cart.item(it).quantity }
                    val request = makeRequest(ready.requestKind, targets)
                    start(
                        SentRequest(
                            id = UUID.randomUUID(), generation = generation, request = request,
                            targets = targets,
                            revisions = ready.ids.associateWith { cart.item(it).revision },
                            commandIds = ready.commandIds, requestKind = ready.requestKind,
                        )
                    )
                    return
                }

                null -> break
            }
        }

        scheduler.armNextDeadline()
    }

    private fun makeRequest(kind: RequestKind, targets: Map<Long, Int>): CartRequest {
        return when (kind) {
            RequestKind.ADD_MULTIPLE -> CartRequest.AddProducts(targets)
            RequestKind.REPLACE_BOTTLES -> CartRequest.ReplaceBottles(targets)
            RequestKind.AUTO -> {
                if (targets.size > 1) CartRequest.AddProducts(targets)
                else {
                    val (id, quantity) = targets.entries.single()
                    if (cart.item(id).confirmedQuantity == 0 && quantity > 0) {
                        CartRequest.AddProduct(id, quantity)
                    } else {
                        CartRequest.UpdateProduct(id, quantity)
                    }
                }
            }
        }
    }

    private fun start(sent: SentRequest) {
        inFlight = sent
        cart.markSending(sent.targets.keys)
        requestJob = scope.launch {
            val result: Result<Unit> = try {
                repository.execute(sent.request)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(error)
            }
            inbox.send(InboxMessage.NetworkCompleted(sent, result))
        }
    }
}
