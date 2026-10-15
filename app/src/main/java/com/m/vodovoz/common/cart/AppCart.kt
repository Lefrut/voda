package com.m.vodovoz.common.cart

import com.m.vodovoz.common.di.DefaultDispatcher
import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

enum class DispatchMode { DEBOUNCED, IMMEDIATE }

/** Для REPLACE_BOTTLES передавай всю группу, включая нули для удаляемых бутылей. */
enum class RequestKind { ADD_OR_CHANGE, ADD_MULTIPLE, REPLACE_BOTTLES }
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

/** Applied содержит подтверждённое количество перед запросом и принятое сервером количество. */
sealed interface CartItemChange {
    data class Applied(val previousQuantity: Int, val quantity: Int) : CartItemChange
    data class NoChange(val quantity: Int) : CartItemChange
    data object Superseded : CartItemChange
    data class Failed(val error: Throwable) : CartItemChange
    data object Reset : CartItemChange
}

data class CartChangeResult(val items: Map<Long, CartItemChange>) {
    fun item(id: Long): CartItemChange = items[id] ?: CartItemChange.Reset
}

/**
 * Хранит последнюю ожидающую команду для каждого ID. Один срок отправки обновляется
 * каждым действием пользователя; Flow сообщает менеджеру, когда он наступил.
 * Все методы вызываются из последовательного цикла AppCartManager.
 */


@Singleton
class AppCartManager(
    parentScope: CoroutineScope,
    private val repository: CartRepository,
    private val onResult: suspend (CartResult) -> Unit = {},
    private val nowMillis: () -> Long = { System.nanoTime() / 1_000_000L },
    debounceMillis: Long = 300L
) {
    @Inject
    constructor(
        repository: CartRepository,
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ) : this(
        parentScope = CoroutineScope(SupervisorJob() + dispatcher),
        repository = repository,
    )

    /** Зафиксированный снимок уже отправленного запроса: нужен при позднем ответе. */
    private data class SentRequest(
        val id: UUID,
        val generation: Long,
        val request: CartRequest,
        val targets: Map<Long, Int>,
        val previousQuantities: Map<Long, Int>,
        val revisions: Map<Long, Long>,
        val commandIds: Set<UUID>,
        val requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
        val commandsById: Map<Long, UUID> = emptyMap(),
    )

    private data class AwaitingAction(
        val done: CompletableDeferred<CartChangeResult>,
        val remainingIds: MutableSet<Long>,
        val results: MutableMap<Long, CartItemChange> = mutableMapOf(),
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
        data class LoadCart(
            val items: Map<Long, Int>,
            val done: CompletableDeferred<Unit>
        ) : InboxMessage

        data class SyncIfIdle(
            val items: Map<Long, Int>,
            val expectedRevision: Long,
            val done: CompletableDeferred<Boolean>,
        ) : InboxMessage

        data class AwaitIdle(val done: CompletableDeferred<Long>) : InboxMessage

        data class UserAction(
            val id: UUID,
            val command: Command,
            val done: CompletableDeferred<CartChangeResult>? = null,
        ) : InboxMessage
        data class NetworkCompleted(val sent: SentRequest, val result: Result<Unit>) : InboxMessage
        data object TimerExpired : InboxMessage
    }

    private val scope =
        CoroutineScope(parentScope.coroutineContext + SupervisorJob(parentScope.coroutineContext[Job]))
    private val inbox = Channel<InboxMessage>(Channel.UNLIMITED)
    private val cart = Cart()
    val itemsState: StateFlow<Map<Long, CartItem>> get() = cart.itemsState
    val quantities = itemsState.map { items -> items.mapValues { it.value.quantity } }
        .distinctUntilChanged()
    val loadingProductIds = itemsState.map { items -> items.filterValues { it.isLoading }.keys }
        .distinctUntilChanged()

    /** Меняется при принятии команд, завершении запросов и применении серверного снимка. */
    @Volatile
    var revision: Long = 0L
        private set

    private val _results = MutableSharedFlow<CartResult>(extraBufferCapacity = 64)
    val results: SharedFlow<CartResult> = _results.asSharedFlow()

    private val scheduler = CartScheduler(
        cart = cart,
        debounceMillis = debounceMillis.coerceAtLeast(0L),
        nowMillis = nowMillis,
    )

    private val inFlight = mutableMapOf<UUID, SentRequest>()
    private val requestJobs = mutableMapOf<UUID, Job>()
    private val awaitingActions = mutableMapOf<UUID, AwaitingAction>()
    private val awaitingClears = mutableMapOf<UUID, AwaitingAction>()
    private val idleWaiters = mutableListOf<CompletableDeferred<Long>>()
    private var generation = 0L
    private var loaded = false
    private val waitingForLoad = ArrayDeque<InboxMessage.UserAction>()

    init {
        scope.coroutineContext[Job]?.invokeOnCompletion { inbox.close() }
        observeInbox()
        observeScheduler()
    }

    /** Flow только будит Inbox: состояния Cart и очереди меняет один обработчик. */
    private fun observeScheduler() = scope.launch {
        scheduler.wakeups.collectLatest { wakeup ->
            val atMillis = wakeup.atMillis ?: return@collectLatest
            delay((atMillis - nowMillis()).coerceAtLeast(0L))
            inbox.send(InboxMessage.TimerExpired)
        }
    }

    private fun observeInbox() = scope.launch {
        for (message in inbox) {
            when (message) {
                is InboxMessage.LoadCart -> load(message)
                is InboxMessage.SyncIfIdle -> syncIfIdle(message)
                is InboxMessage.AwaitIdle -> idleWaiters.add(message.done)
                is InboxMessage.UserAction -> if (loaded) accept(message) else waitingForLoad.addLast(
                    message
                )

                is InboxMessage.NetworkCompleted -> finish(message)
                InboxMessage.TimerExpired -> Unit
            }
            if (loaded) dispatchReady()
            completeIdleWaiters()
        }
    }


    /**
     * Вызывай при старте приложения или смене пользователя после получения корзины.
     * Повторная загрузка сбрасывает очередь и игнорирует ответы прошлого поколения.
     */
    suspend fun loadCart(quantities: Map<Long, Int>) {
        if (scope.coroutineContext[Job]?.isActive != true) return
        val done = CompletableDeferred<Unit>(scope.coroutineContext[Job])
        val items = quantities.mapValues { it.value.coerceAtLeast(0) }
        if (inbox.trySend(InboxMessage.LoadCart(items, done)).isFailure) {
            done.cancel()
            return
        }
        done.await()
    }

    /** expectedRevision нужно получить до загрузки снимка, чтобы отклонить устаревший ответ. */
    suspend fun syncIfIdle(quantities: Map<Long, Int>, expectedRevision: Long): Boolean {
        if (scope.coroutineContext[Job]?.isActive != true) return false
        val done = CompletableDeferred<Boolean>(scope.coroutineContext[Job])
        val items = quantities.mapValues { it.value.coerceAtLeast(0) }
        if (inbox.trySend(InboxMessage.SyncIfIdle(items, expectedRevision, done)).isFailure) {
            done.cancel()
            return false
        }
        return done.await()
    }

    /** Дожидается всей очереди и возвращает версию перед загрузкой деталей корзины. */
    suspend fun awaitIdle(): Long {
        val done = CompletableDeferred<Long>(scope.coroutineContext[Job])
        if (inbox.trySend(InboxMessage.AwaitIdle(done)).isFailure) done.cancel()
        return done.await()
    }

    private fun completeIdleWaiters() {
        idleWaiters.removeAll { it.isCompleted }
        if (inFlight.isNotEmpty() || scheduler.hasPending()) return
        idleWaiters.forEach { it.complete(revision) }
        idleWaiters.clear()
    }

    suspend fun setQuantity(
        id: Long,
        quantity: Int,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
    ): CartItemChange = setQuantities(mapOf(id to quantity), mode, requestKind).item(id)

    suspend fun setQuantities(
        targets: Map<Long, Int>,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
    ): CartChangeResult {
        if (targets.isEmpty()) return CartChangeResult(emptyMap())
        return submitAwait(Command.Set(targets.mapValues { it.value.coerceAtLeast(0) }, mode, requestKind))
    }

    suspend fun adjustQuantity(
        id: Long,
        delta: Int,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.ADD_OR_CHANGE,
    ): CartItemChange = submitAwait(Command.Add(mapOf(id to delta), mode, requestKind)).item(id)

    suspend fun incrementProducts(
        deltas: Map<Long, Int>,
        mode: DispatchMode = DispatchMode.DEBOUNCED,
        requestKind: RequestKind = RequestKind.ADD_MULTIPLE,
    ): CartChangeResult {
        val positiveDeltas = deltas.filterValues { it > 0 }
        if (positiveDeltas.isEmpty()) return CartChangeResult(emptyMap())
        return submitAwait(Command.Add(positiveDeltas, mode, requestKind))
    }

    suspend fun clear(): CartChangeResult = submitAwait(Command.Clear)

    fun close() {
        inbox.close()
        scope.cancel()
    }

    private suspend fun submitAwait(command: Command): CartChangeResult {
        val ids = when (command) {
            is Command.Set -> command.targets.keys
            is Command.Add -> command.deltas.keys
            Command.Clear -> emptySet()
        }
        val reset = CartChangeResult(ids.associateWith { CartItemChange.Reset })
        if (scope.coroutineContext[Job]?.isActive != true) return reset
        val done = CompletableDeferred<CartChangeResult>(scope.coroutineContext[Job])
        if (inbox.trySend(InboxMessage.UserAction(UUID.randomUUID(), command, done)).isFailure) {
            done.cancel()
            return reset
        }
        return done.await()
    }

    private fun load(message: InboxMessage.LoadCart) {
        generation++
        revision++
        (awaitingActions.values + awaitingClears.values).forEach { action ->
            action.remainingIds.forEach { action.results[it] = CartItemChange.Reset }
            action.done.complete(CartChangeResult(action.results.toMap()))
        }
        awaitingActions.clear()
        awaitingClears.clear()
        requestJobs.values.forEach { it.cancel() }
        requestJobs.clear()
        inFlight.clear()
        scheduler.reset()
        cart.loadFromServer(message.items)
        loaded = true
        while (waitingForLoad.isNotEmpty()) accept(waitingForLoad.removeFirst())
        message.done.complete(Unit)
    }

    private fun syncIfIdle(message: InboxMessage.SyncIfIdle) {
        if (message.expectedRevision != revision) {
            message.done.complete(false)
            return
        }
        if (!loaded) {
            cart.loadFromServer(message.items)
            revision++
            loaded = true
            while (waitingForLoad.isNotEmpty()) accept(waitingForLoad.removeFirst())
            message.done.complete(true)
            return
        }

        val idle = inFlight.isEmpty() && !scheduler.hasPending()
        if (idle) {
            cart.loadFromServer(message.items)
            revision++
        }
        message.done.complete(idle)
    }

    private fun accept(message: InboxMessage.UserAction) {
        revision++
        when (val command = message.command) {
            is Command.Set -> {
                acceptItems(message, command.targets, command.mode, command.kind)
            }

            is Command.Add -> {
                val targets = command.deltas.mapValues { (id, delta) ->
                    val quantity = cart.item(id).quantity.toLong() + delta
                    quantity.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
                }
                acceptItems(message, targets, command.mode, command.kind)
            }

            Command.Clear -> {
                scheduler.takeClear()?.let { previous ->
                    completeClear(previous.commandId) { CartItemChange.Superseded }
                }
                message.done?.let { done ->
                    awaitingClears[message.id] = AwaitingAction(done, cart.ids().toMutableSet())
                }
                scheduler.enqueueClear(message.id, cart.clear()).forEach { (id, commandId) ->
                    completeItem(commandId, id, CartItemChange.Superseded)
                }
            }
        }
    }

    private fun acceptItems(
        message: InboxMessage.UserAction,
        targets: Map<Long, Int>,
        mode: DispatchMode,
        kind: RequestKind,
    ) {
        message.done?.let { done ->
            awaitingActions[message.id] = AwaitingAction(
                done = done,
                remainingIds = targets.keys.toMutableSet(),
            )
        }
        cart.setQuantities(targets)
        scheduler.enqueue(targets.keys, mode, kind, message.id).forEach { (id, commandId) ->
            completeItem(commandId, id, CartItemChange.Superseded)
        }
    }

    private fun completeItem(commandId: UUID, id: Long, result: CartItemChange) {
        val action = awaitingActions[commandId] ?: return
        if (!action.remainingIds.remove(id)) return
        action.results[id] = result
        if (action.remainingIds.isEmpty()) {
            awaitingActions.remove(commandId)
            action.done.complete(CartChangeResult(action.results.toMap()))
        }
    }

    private fun finish(message: InboxMessage.NetworkCompleted) {
        val sent = message.sent
        if (sent.generation != generation || inFlight.remove(sent.id) == null) return
        revision++
        requestJobs.remove(sent.id)
        cart.applyResult(sent.targets, sent.revisions, message.result)
        if (sent.request is CartRequest.Clear) {
            sent.commandIds.forEach { commandId ->
                completeClear(commandId) { id ->
                    message.result.fold(
                        onSuccess = { CartItemChange.Applied(sent.previousQuantities[id] ?: 0, 0) },
                        onFailure = { CartItemChange.Failed(it) },
                    )
                }
            }
        }
        sent.commandsById.forEach { (id, commandId) ->
            if (commandId !in awaitingActions) return@forEach
            val outcome = message.result.fold(
                onSuccess = {
                    CartItemChange.Applied(
                        sent.previousQuantities[id] ?: 0,
                        sent.targets[id] ?: cart.item(id).quantity,
                    )
                },
                onFailure = { CartItemChange.Failed(it) },
            )
            completeItem(commandId, id, outcome)
        }
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
                message.result.exceptionOrNull() ?: IllegalStateException("Cart request failed"),
            )
        }
        _results.tryEmit(outcome)
        scope.launch { onResult(outcome) }
        scheduler.wakeAfterResult()
    }

    private fun completeClear(commandId: UUID, result: (Long) -> CartItemChange) {
        val action = awaitingClears.remove(commandId) ?: return
        action.done.complete(CartChangeResult(action.remainingIds.associateWith(result)))
    }

    private fun dispatchReady() {
        if (inFlight.values.any { it.request is CartRequest.Clear }) return

        if (scheduler.hasClear()) {
            if (inFlight.isNotEmpty()) return
            val clear = scheduler.takeClear() ?: return
            val targets = cart.ids().associateWith { 0 }
            start(
                SentRequest(
                    id = UUID.randomUUID(), generation = generation,
                    request = CartRequest.Clear, targets = targets,
                    previousQuantities = targets.keys.associateWith { cart.item(it).confirmedQuantity },
                    revisions = clear.revisions, commandIds = setOf(clear.commandId),
                )
            )
            return
        }

        val busyIds = inFlight.values.flatMapTo(mutableSetOf()) { it.targets.keys }
        val batch = scheduler.takeReady(busyIds) ?: return
        cart.settleNoops(batch.noops.keys)
        batch.noops.forEach { (id, commandId) ->
            completeItem(commandId, id, CartItemChange.NoChange(cart.item(id).quantity))
        }
        batch.requests.forEach { ready ->
            val targets = ready.ids.associateWith { cart.item(it).quantity }
            val request = makeRequest(ready.requestKind, targets) ?: return@forEach
            start(
                SentRequest(
                    id = UUID.randomUUID(), generation = generation,
                    request = request,
                    targets = targets,
                    previousQuantities = ready.ids.associateWith { cart.item(it).confirmedQuantity },
                    revisions = ready.ids.associateWith { cart.item(it).revision },
                    commandIds = ready.commandIds, requestKind = ready.requestKind,
                    commandsById = ready.commandsById,
                )
            )
        }
    }

    private fun makeRequest(kind: RequestKind, targets: Map<Long, Int>): CartRequest? {
        if (targets.isEmpty()) return null
        return when (kind) {
            RequestKind.ADD_MULTIPLE -> CartRequest.AddProducts(targets)
            RequestKind.REPLACE_BOTTLES -> CartRequest.ReplaceBottles(targets.filterValues { it > 0 })
            RequestKind.ADD_OR_CHANGE -> {
                if (targets.size > 1) CartRequest.AddProducts(targets)
                else {
                    val (id, quantity) = targets.entries.firstOrNull() ?: return null
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
        inFlight[sent.id] = sent
        cart.markSending(sent.targets.keys, sent.revisions)
        requestJobs[sent.id] = scope.launch {
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
