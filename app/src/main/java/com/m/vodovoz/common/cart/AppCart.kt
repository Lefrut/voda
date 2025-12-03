package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/** Задача:
 *
 *
 * */

private suspend fun clientMethod() {

}

private abstract class AbstractAppCart(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cartStrategyFactory: VodovozCartStrategyFactory = VodovozCartStrategyFactory(
        vodovozServiceRepository
    ),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppCart {


    private val updatingMutex = Mutex()
    private val scope = CoroutineScope(dispatcher)
    private var version by AtomicIntDelegate(0)

    private val _items = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    override val items = _items.asStateFlow()

    private val _operationsByVersionFlow =
        MutableStateFlow<Map<Int, List<OperationInfo>>>(emptyMap())

    private fun getOperationsByVersion(version: Int): List<OperationInfo> {
        return _operationsByVersionFlow.value[version] ?: emptyList()
    }

    @OptIn(FlowPreview::class)
    private val operationsExecutor = _operationsByVersionFlow
        .map { getOperationsByVersion(version) }
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .debounce(350)
        .onEach { operationInfos ->

        }
        .launchIn(scope)

    private val _itemsSharedFlow = MutableSharedFlow<Map<Long, CartItem>>(1)

    protected suspend fun updateItems(
        block: Map<Long, CartItem>.() -> Map<Long, CartItem>,
    ): Map<Long, CartItem> = updatingMutex.withLock {
        val items = _itemsSharedFlow.firstOrNull() ?: return emptyMap()
        val updatedItems = block(items)
        _itemsSharedFlow.emit(updatedItems)
        return updatedItems
    }

    override suspend fun addProduct(
        id: Long,
        quantity: Int,
    ) = putOperation(
        type = OperationType.Add,
        item = CartItem.from(id, quantity)
    )

    override suspend fun addProducts(
        idsAndQuantities: Map<Long, Int>
    ) = idsAndQuantities.forEach { (id, quantity) ->
        putOperation(
            type = OperationType.Add,
            item = CartItem.from(id, quantity)
        )
    }

    override suspend fun changeProducts(
        idsAndQuantities: Map<Long, Int>,
    ) = idsAndQuantities.forEach { (id, quantity) ->
        putOperation(
            type = OperationType.Change,
            item = CartItem.from(id, quantity)
        )
    }

    override suspend fun changeProduct(id: Long, quantity: Int) = putOperation(
        type = OperationType.Change,
        item = CartItem.from(id, quantity)
    )

    override suspend fun clear() = putOperation(
        type = OperationType.Clear,
        incomingItems = emptyList()
    )

    protected suspend fun putOperation(
        type: OperationType,
        item: CartItem,
    ) = putOperation(type = type, incomingItems = listOf(item))

    protected suspend fun putOperation(
        type: OperationType,
        incomingItems: List<CartItem>,
    ) {
        val noLoadingIncommingItems = incomingItems.filter { item ->
            items.value[item.id]?.isLoading == false
        }
        if (noLoadingIncommingItems.isEmpty()) return

        val operationInfo = OperationInfo(
            incommingItems = incomingItems,
            type = type,
        )

        updatingMutex.withLock {
            val currentVersion = version
            val updatedOperations = getOperationsByVersion(
                currentVersion
            ).plus(operationInfo)
            _operationsByVersionFlow.update { operationsByVersion ->
                operationsByVersion.mapValues { (version, operations) ->
                    if (currentVersion == version) updatedOperations
                    else operations
                }
            }
        }
    }
}


private data class CartItem(
    val id: Long,
    val isLoading: Boolean,
    val quantity: Int,
) {
    companion object {
        fun from(id: Long, quantity: Int): CartItem {
            return CartItem(id, false, quantity)
        }

        fun from(idsAndQuantities: Map<Long, Int>): List<CartItem> {
            return idsAndQuantities.map { (id, quantity) ->
                from(id, quantity)
            }
        }
    }
}

private interface CartStrategy {

    suspend fun apply(
        items: List<CartItem>,
        incomingItems: List<CartItem>
    ): List<CartItem>

    suspend fun sync(
        items: List<CartItem>
    ): Result<Unit>
}

private class VodovozCartStrategyFactory(
    private val vodovozServiceRepository: VodovozServiceRepository
) {
    fun createCartStrategy(
        onApply: (items: List<CartItem>, incomingItems: List<CartItem>) -> List<CartItem>,
        onSync: suspend (List<CartItem>) -> Result<Unit>,
    ) = object : CartStrategy {
        override suspend fun apply(
            items: List<CartItem>,
            incomingItems: List<CartItem>
        ): List<CartItem> = onApply(items, incomingItems)

        override suspend fun sync(items: List<CartItem>): Result<Unit> = onSync(items)
    }

    private val cartStrategyMap = mapOf<OperationType, CartStrategy>(
        OperationType.Add to createCartStrategy(
            onApply = { items, incomingItems ->
                combine(items, incomingItems) { quantity1, quantity2 ->
                    quantity1 + quantity2
                }
            },
            onSync = { items ->
                val firstItems = items.first()
                vodovozServiceRepository.addProductToCart(
                    firstItems.id,
                    firstItems.quantity
                ).singleUnitResult()
            }
        ),
        OperationType.Change to createCartStrategy(
            onApply = { items, incomingItems ->
                combine(items, incomingItems) { _, q2 -> q2 }
            },
            onSync = { items ->
                val firstItems = items.first()
                vodovozServiceRepository.updateProductInCart(
                    firstItems.id,
                    firstItems.quantity
                ).singleUnitResult()
            }
        ),
        OperationType.Clear to createCartStrategy(
            onApply = { _, _ -> emptyList() },
            onSync = {
                vodovozServiceRepository.clearCart().singleUnitResult()
            }
        )

    )


    operator fun get(operationType: OperationType): CartStrategy {
        return cartStrategyMap[operationType]!!
    }

    suspend fun <T> Flow<Result<T>>.singleUnitResult(): Result<Unit> {
        return singleResult().mapCatching { Unit }
    }

    private fun combine(
        existingItems: List<CartItem>,
        incomingItems: List<CartItem>,
        transformQuantities: (Int, Int) -> Int
    ): List<CartItem> {
        val existingMap = existingItems.associateBy { it.id }
        val incomingMap = incomingItems.associateBy { it.id }

        val allIds = (existingMap.keys + incomingMap.keys)

        return allIds.mapNotNull { id ->
            val existing = existingMap[id]
            val incoming = incomingMap[id]

            val q1 = existing?.quantity ?: 0
            val q2 = incoming?.quantity ?: 0

            val finalQuantity = transformQuantities(q1, q2)

            existing?.copy(
                quantity = finalQuantity
            ) ?: incoming?.copy(quantity = finalQuantity)
        }
    }

}

private fun List<CartItem>.format(): String {
    return associate { it.id to it.quantity }.toCartProducts().productsIdsWithQuantity
}

private data class OperationInfo(
    val incommingItems: List<CartItem>,
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val state: OperationState = OperationState.Loading,

    ) {
    init {
        check(incommingItems.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItem by lazy { incommingItems.first() }

}

enum class OperationType {
    Add, Change, Clear;
}

enum class OperationState {
    Loading,
    Success,
    Error,
}


private interface AppCart {

    val items: StateFlow<Map<Long, CartItem>>

    suspend fun addProduct(id: Long, quantity: Int)

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>)

    suspend fun changeProduct(id: Long, quantity: Int)

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>)

    suspend fun clear()
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
        value: Int
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
