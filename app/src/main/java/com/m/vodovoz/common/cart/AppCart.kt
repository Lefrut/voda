package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.setAll
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import kotlin.math.max
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.milliseconds


/** Задача:
 *
 *
 * */


open class AbstractAppCart(
    private val cartRepository: CartRepository,
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
    private val operations get() = _operationsFlow.value

    @OptIn(FlowPreview::class)
    private val shedulingOperations = _operationsFlow
        .filter { it.isNotEmpty() }
        .debounce {
            val isImmediate = it.any { op ->
                op.startPolicy == AppCart.StartPolicy.IMMEDIATE
            }
            if (isImmediate) {
                0.milliseconds
            } else {
                375.milliseconds
            }
        }
        .map { currentOperations ->
            updatingMutex.withLock {
                val ids = currentOperations.flatMap { op ->
                    op.itemChanges.keys
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
            operationInfos.map { operationInfo ->
                scope.async {
                    operationInfo to cartRepository.execute(operationInfo)
                }
            }
        }
        .onEach { deferreds ->

            updatingMutex.withLock {
                val operationAndResultPairs = deferreds.awaitAll()
                val currentItems = items

                val actualItems = operationAndResultPairs.flatMap { (operationInfo, result) ->
                    operationInfo.itemChanges.values.map { change ->
                        val currentItem = currentItems.getOrDefault(
                            change.productId,
                            CartItem.from(change.productId)
                        )
                        currentItem.copy(
                            quantity = if (result.isSuccess) change.targetQuantity else change.baseQuantity,
                            isLoading = false
                        )
                    }
                }
                _itemsState.setAll(actualItems.associateBy { it.id })
            }
        }
        .launchIn(scope)


    private var itemsSnaphot: Map<Long, CartItem>? = null


    private fun scheduleOperation(
        id: Long,
        quantity: Int,
        requestType: CartRequestType,
        conflictStrategy: OperationConflictStrategy,
    ) = scheduleOperation(mapOf(id to quantity), requestType, conflictStrategy)

    private fun scheduleOperation(
        itemsMap: Map<Long, Int>,
        requestType: CartRequestType,
        conflictStrategy: OperationConflictStrategy
    ) = scope.launch {
        updatingMutex.withLock {
            if (itemsSnaphot == null) {
                itemsSnaphot = items.toMap()
            }

            val itemsAndChanges = itemsMap.mapNotNull { (id, quantity) ->
                items.getOrDefault(id, CartItem.from(id)).let { item ->
                    if (item.isLoading) return@mapNotNull null
                    val baseQuantity = item.quantity
                    val targetQuantity = max(0, quantity + baseQuantity)

                    item.copy(quantity = targetQuantity) to PendingItemChange(
                        productId = item.id,
                        baseQuantity = baseQuantity,
                        targetQuantity = targetQuantity
                    )
                }
            }

            _itemsState.setAll(
                itemsAndChanges.associate { itemAndChange ->
                    val item = itemAndChange.first
                    item.id to item
                }
            )

            val operationInfo = OperationInfo(
                itemChanges = itemsAndChanges.associate { pair ->
                    val change = pair.second
                    change.productId to change
                },
                requestType = requestType,
                startPolicy = startPolicy
            )

            val mutableOperations = operations.toMutableList()
            conflictStrategy.apply(
                queue = mutableOperations,
                incoming = operationInfo
            )
            _operationsFlow.value = mutableOperations
        }

    }

    override fun incrementProduct(id: Long, quantity: Int): Job = scheduleOperation(
        id = id,
        quantity = quantity,
        requestType = CartRequestType.Add,
        conflictStrategy = OperationConflictStrategy.Replace
    )

    override fun decrementProduct(id: Long, quantity: Int): Job = scheduleOperation(
        id = id,
        quantity = -quantity,
        requestType = CartRequestType.Change,
        conflictStrategy = OperationConflictStrategy.Replace
    )


    override fun incrementProducts(itemsMap: Map<Long, Int>) = scheduleOperation(
        itemsMap = itemsMap,
        requestType = CartRequestType.AddMultiple,
        conflictStrategy = OperationConflictStrategy.Replace
    )

    override fun clear(): Job = scheduleOperation(
        itemsMap = items.mapValues { entry -> entry.value.quantity },
        requestType = CartRequestType.Clear,
        conflictStrategy = OperationConflictStrategy.Clear
    )

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
    val baseQuantity: Int,
    val targetQuantity: Int,
) {
    val delta: Int get() = targetQuantity - baseQuantity
}


private fun PendingItemChange.toMap(): Map<Long, PendingItemChange> {
    return listOf(this).toMap()
}

private fun List<PendingItemChange>.toMap(): Map<Long, PendingItemChange> {
    return associateBy { it.productId }
}


data class OperationInfo(
    val itemChanges: Map<Long, PendingItemChange>,
    val requestType: CartRequestType,
    val startPolicy: AppCart.StartPolicy,
    val id: String = UUID.randomUUID().toString(),
) {
    init {
        check(itemChanges.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItemChange get() = itemChanges.values.first()

}


sealed interface OperationConflictStrategy {

    fun apply(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
        queue.defaultMerge(incoming)
    }

    data object Replace : OperationConflictStrategy

    data object Clear : OperationConflictStrategy {
        override fun apply(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
            queue.clear()
            queue.add(incoming)
        }
    }

}

private fun MutableList<OperationInfo>.defaultMerge(incoming: OperationInfo) {
    val prev = toList()
    clear()
    val list = prev.mapNotNull { listOperationInfo ->
        listOperationInfo.copy(
            itemChanges = listOperationInfo.itemChanges.filter { entry ->
                !incoming.itemChanges.containsKey(entry.key)
            }.ifEmpty { return@mapNotNull null }
        )
    }
    addAll(list)
    add(incoming)
}

sealed interface CartRequestType {
    data object Add : CartRequestType
    data object AddMultiple : CartRequestType
    data object Change : CartRequestType
    data object ReplaceBottles : CartRequestType
    data object Clear : CartRequestType
}

interface CartRepository {
    suspend fun execute(operationInfo: OperationInfo): Result<Unit>
}

class VodovozCartRepository(
    private val vodovozServiceRepository: VodovozServiceRepository
) : CartRepository {

    override suspend fun execute(
        operationInfo: OperationInfo
    ): Result<Unit> {
        val (id, quantity) = with(operationInfo.firstItemChange) {
            productId to targetQuantity
        }
        val cartProducts = operationInfo.itemChanges.mapValues {
            it.value.targetQuantity
        }.toCartProducts()

        return when (operationInfo.requestType) {
            CartRequestType.Add -> vodovozServiceRepository.addProductToCart(
                productId = id,
                quantity = quantity
            )

            CartRequestType.AddMultiple -> vodovozServiceRepository.addMultipleProductsToCart(
                cartProducts.productsIdsWithQuantity
            )

            CartRequestType.Change -> vodovozServiceRepository.updateProductInCart(
                productId = id,
                quantity = quantity
            )

            CartRequestType.ReplaceBottles -> vodovozServiceRepository.replaceMultipleBottlesToCart(
                cartProducts
            )

            CartRequestType.Clear -> vodovozServiceRepository.clearCart()
        }.singleResult().mapCatching { }
    }


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
