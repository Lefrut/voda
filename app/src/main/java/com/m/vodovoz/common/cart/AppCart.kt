package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.set
import com.m.vodovoz.util.setIfPresent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.filter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.milliseconds


/** Задача:
 *
 *
 * */

private suspend fun clientMethod() {

}

open class AbstractAppCart(
    private val vodovozServiceRepository: VodovozServiceRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppCart {
    private val updatingMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var version by AtomicIntDelegate(0)

    private val _itemsState = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    override val itemsState = _itemsState.asStateFlow()

    private val items
        get() = _itemsState.value

    private var _startPolicyState = MutableStateFlow(AppCart.StartPolicy.IMMEDIATE)

    override val startPolicy: AppCart.StartPolicy get() = _startPolicyState.value

    override fun setStartPolicy(policy: AppCart.StartPolicy) {
        _startPolicyState.update { policy }
    }

    private val _operationsFlow =
        MutableStateFlow<List<OperationInfo>>(emptyList())

    private val operations = _operationsFlow.value

    //todo
    private val addingRules = listOf<(OperationInfo) -> Boolean> { info ->
        info.items.isNotEmpty()
    }

    //todo
    private val passingRules = listOf<(OperationInfo, OperationInfo) -> Boolean> { p1, p2 ->
        p1.items.containsKey(p2.items.keys.first())
    }


    @OptIn(FlowPreview::class)
    private val operationsExecutor = _operationsFlow
        .filter { operations.isNotEmpty() }
        .distinctUntilChanged()
        .debounce {
            val isImmediate = operations.any { op ->
                op.startPolicy == AppCart.StartPolicy.IMMEDIATE
            }

            if (isImmediate) {
                0.milliseconds
            } else {
                375.milliseconds
            }
        }
        .map {
            updatingMutex.withLock {
                val currentOperations = operations
                val ids = currentOperations.flatMap { op ->
                    op.items.keys
                }
                itemsSnaphot = null
                _operationsFlow.value = emptyList()
                _itemsState.value = items.mapValues { entry ->
                    val item = entry.value
                    item.copy(
                        isLoading = ids.any { id -> id == item.id }
                    )
                }

                currentOperations
            }
        }
        .map { operationInfos ->
            operationInfos.map { opInfo ->
                val (id, quantity) = with(opInfo.firstItem) {
                    productId to targetQty
                }
                scope.async {
                    opInfo to when (opInfo.requestType) {
                        OperationRequestType.Add -> vodovozServiceRepository.addProductToCart(
                            productId = id,
                            quantity = quantity
                        )
                        //todo
                        OperationRequestType.AddMultiple -> vodovozServiceRepository.addProductToCart(
                            productId = id,
                            quantity = quantity
                        )

                        OperationRequestType.Change -> vodovozServiceRepository.updateProductInCart(
                            productId = id,
                            quantity = quantity
                        )
                        //todo
                        OperationRequestType.ReplaceBottles -> vodovozServiceRepository.addProductToCart(
                            productId = id,
                            quantity = quantity
                        )

                        OperationRequestType.Clear -> vodovozServiceRepository.clearCart()
                    }.singleResult()
                }
            }
        }
        .onEach { deferreds ->
            deferreds.map { deffered ->
                scope.launch {
                    val (operationInfo, result) = deffered.await()
                    val changes = operationInfo.items.values

                    updatingMutex.withLock {
                        val mutaleItems = items.toMutableMap()
                        val actualItems = changes.associate { change ->
                            val item = mutaleItems.getOrDefault(
                                key = change.productId,
                                defaultValue = CartItem.from(change.productId)
                            )
                            item.id to item.copy(
                                isLoading = false,
                                quantity = if (result.isSuccess) {
                                    item.quantity
                                } else {
                                    change.baseQty
                                }
                            )
                        }
                        mutaleItems.putAll(actualItems)
                        _itemsState.value = mutaleItems
                    }
                }
            }
        }
        .launchIn(scope)


    private var itemsSnaphot: Map<Long, CartItem>? = null


    private fun doOperation(
        block: () -> Unit
    ) = scope.launch {
        updatingMutex.withLock {
            if (itemsSnaphot == null) {
                itemsSnaphot = items.toMap()
            }
        }
    }

    override fun incrementProduct(id: Long, quantity: Int): Job = scope.launch {
        updatingMutex.withLock {
            if (itemsSnaphot == null) {
                itemsSnaphot = items.toMap()
            }

            //todo - can create func
            val (currentCartItem, baseQuantity, targetQuantity) = items.getOrDefault(
                key = id, defaultValue = CartItem.from(id)
            ).let { item ->
                val targetQuantity = quantity + item.quantity
                Triple(
                    first = item.copy(quantity = targetQuantity),
                    second = item.quantity,
                    third = targetQuantity
                )
            }
            if (currentCartItem.isLoading) return@launch

            _itemsState.set(id, currentCartItem)

            val requestType = if (itemsSnaphot?.get(id) == null) {
                OperationRequestType.Add
            } else OperationRequestType.ReplaceBottles

            val operationInfo = OperationInfo(
                items = PendingItemChange(
                    productId = id,
                    baseQty = baseQuantity,
                    targetQty = targetQuantity
                ).toMap(),
                requestType = requestType,
                startPolicy = startPolicy
            )

            val mutableOperations = operations.toMutableList()
            OperationMergeType.Replace.merge(
                queue = mutableOperations,
                incoming = operationInfo
            )
            _operationsFlow.value = mutableOperations
        }
    }

    override fun decrementProduct(id: Long, quantity: Int): Job {
        TODO("Not yet implemented")
    }

    override fun incrementProducts(itemsMap: Map<Long, Int>): Job {
        TODO("Not yet implemented")
    }

    override fun clear(): Job {
        TODO("Not yet implemented")
    }
}


data class CartItem(
    val id: Long,
    val isLoading: Boolean,
    val quantity: Int,
) {
    companion object {
        fun from(id: Long, quantity: Int): CartItem {
            return CartItem(id, false, quantity)
        }

        fun from(id: Long): CartItem {
            return CartItem(id, false, 0)
        }


        fun from(idsAndQuantities: Map<Long, Int>): Map<Long, CartItem> {
            return idsAndQuantities.map { (id, quantity) ->
                from(id, quantity)
            }.associateBy { it.id }
        }
    }
}


private fun List<CartItem>.format(): String {
    return associate { it.id to it.quantity }.toCartProducts().productsIdsWithQuantity
}


data class PendingItemChange(
    val productId: Long,
    val baseQty: Int,
    val targetQty: Int,
) {
    val delta: Int get() = targetQty - baseQty
}


private fun PendingItemChange.toMap(): Map<Long, PendingItemChange> {
    return listOf(this).toMap()
}

private fun List<PendingItemChange>.toMap(): Map<Long, PendingItemChange> {
    return associateBy { it.productId }
}


data class OperationInfo(
    val items: Map<Long, PendingItemChange>,
    val requestType: OperationRequestType,
    val startPolicy: AppCart.StartPolicy,
    val id: String = UUID.randomUUID().toString(),
) {
    init {
        check(items.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItem by lazy { items.getValue(0) }

}


sealed interface OperationMergeType {

    fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
        queue.defaultMerge(incoming)
    }

    data object Replace : OperationMergeType

    data object Clear : OperationMergeType {
        override fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
            queue.clear()
            queue.add(incoming)
        }
    }

}

private fun MutableList<OperationInfo>.defaultMerge(incoming: OperationInfo) {
    clear()
    val list = map { operation ->
        operation.copy(
            items = operation.items.filter { entry ->
                !incoming.items.containsKey(entry.key)
            }
        )
    }
    addAll(list)
    add(incoming)
}

enum class OperationRequestType {
    Add, AddMultiple, Change, ReplaceBottles, Clear;
}


private suspend fun AppCart.withPolicy(
    policy: AppCart.StartPolicy = AppCart.StartPolicy.IMMEDIATE,
    block: AppCart.() -> Job
) {
    val policySnapshot = startPolicy
    setStartPolicy(policy)
    block().join()
    setStartPolicy(policySnapshot)
}


interface AppCart {

    enum class StartPolicy {
        QUEUED, IMMEDIATE;
    }

    val startPolicy: StartPolicy

    fun setStartPolicy(policy: StartPolicy)

    val itemsState: StateFlow<Map<Long, CartItem>>

    fun incrementProduct(id: Long, quantity: Int = 1): Job

    fun decrementProduct(id: Long, quantity: Int = 1): Job

    fun incrementProducts(itemsMap: Map<Long, Int>): Job

    fun clear(): Job
}


class AtomicIntDelegate(initial: Int = 0) : ReadWriteProperty<Any?, Int> {

    val atomic = AtomicInteger(initial)

    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return atomic.get()
    }

    @Suppress("NOTHING_TO_INLINE", "OVERRIDE_BY_INLINE")
    override inline fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: Int,
    ) {
        atomic.set(value)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun AtomicIntDelegate.plusAssign(delta: Int) {
    atomic.addAndGet(delta)
}


@Suppress("NOTHING_TO_INLINE")
inline operator fun AtomicIntDelegate.minusAssign(delta: Int) {
    atomic.addAndGet(-delta)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun AtomicIntDelegate.inc(): AtomicIntDelegate {
    atomic.incrementAndGet()
    return this
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun AtomicIntDelegate.dec(): AtomicIntDelegate {
    atomic.decrementAndGet()
    return this
}
