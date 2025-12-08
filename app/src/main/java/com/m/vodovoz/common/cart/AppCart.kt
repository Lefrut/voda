package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration.Companion.milliseconds


/** Задача:
 *
 *
 * */

private suspend fun clientMethod() {

}

private abstract class AbstractAppCart(
    private val cartStrategyFactory: VodovozCartStrategyFactory,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AppCart {


    private val updatingMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var version by AtomicIntDelegate(0)

    private val _itemsState = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    override val itemsState = _itemsState.asStateFlow()
    private val items
        get() = _itemsState.value

    protected suspend fun updateItems(
        block: Map<Long, CartItem>.() -> Map<Long, CartItem>,
    ): Map<Long, CartItem> = updatingMutex.withLock {
        val updatedItems = block(items)
        _itemsState.value = updatedItems
        return updatedItems
    }

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
        .debounce { 375.milliseconds }
        .onEach { operationInfos ->

        }
        .launchIn(scope)


    override suspend fun incrementProduct(id: Long, quantity: Int) {
        val type =
            if (items[id] != null) OperationApplyType.Change
            else OperationApplyType.Add


    }

    override suspend fun decrementProduct(id: Long, quantity: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun clear() = putOperation(
        type = OperationApplyType.Clear,
        incomingItems = emptyList()
    )

    protected suspend fun putOperation(
        type: OperationApplyType,
        item: CartItem,
    ) = putOperation(type = type, incomingItems = listOf(item))

    protected suspend fun putOperation(
        type: OperationApplyType,
        incomingItems: List<CartItem>,
    ) {
        val noLoadingIncommingItems = incomingItems.filter { item ->
            itemsState.value[item.id]?.isLoading == false
        }
        if (noLoadingIncommingItems.isEmpty()) return

        val operationInfo = OperationInfo(
            incomingItems = incomingItems,
            type = type,
        )

        val currentVersion = version
        val updatedOperations = getOperationsByVersion(
            currentVersion
        ).plus(operationInfo)
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
        incomingItems: List<CartItem>,
    ): List<CartItem>

    suspend fun sync(
        items: List<CartItem>,
    ): Result<Unit>
}

private class VodovozCartStrategyFactory(
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    private val cartStrategyMap = mapOf<OperationApplyType, CartStrategy>(
        OperationApplyType.Add to createCartStrategy(
            onApply = { items, incomingItems ->
                combine(items, incomingItems) { quantity1, quantity2 ->
                    quantity1 + quantity2
                }
            },
            onSync = { item ->
                vodovozServiceRepository.addProductToCart(
                    item.id,
                    item.quantity
                )
            }
        ),
        OperationApplyType.Change to createCartStrategy(
            onApply = { items, incomingItems ->
                combine(items, incomingItems) { _, q2 -> q2 }
            },
            onSync = { item ->
                vodovozServiceRepository.updateProductInCart(
                    item.id,
                    item.quantity
                )
            }
        ),
        OperationApplyType.Clear to createCartStrategy(
            onApply = { _, _ -> emptyList() },
            onSync = {
                vodovozServiceRepository.clearCart()
            }
        )

    )


    operator fun get(operationType: OperationApplyType): CartStrategy {
        return cartStrategyMap[operationType]!!
    }


    private fun combine(
        existingItems: List<CartItem>,
        incomingItems: List<CartItem>,
        transformQuantities: (Int, Int) -> Int,
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


    private fun <T : Any> createCartStrategy(
        onApply: (items: List<CartItem>, incomingItems: List<CartItem>) -> List<CartItem>,
        onSync: suspend (CartItem) -> Flow<T>,
    ) = object : CartStrategy {
        override suspend fun apply(
            items: List<CartItem>,
            incomingItems: List<CartItem>,
        ): List<CartItem> = onApply(items, incomingItems)

        override suspend fun sync(items: List<CartItem>): Result<Unit> {
            return runCatching { onSync(items.first()).single() }
        }
    }

}

private fun List<CartItem>.format(): String {
    return associate { it.id to it.quantity }.toCartProducts().productsIdsWithQuantity
}

private data class OperationInfo(
    val incomingItems: List<CartItem>,
    val id: String = UUID.randomUUID().toString(),
    val type: OperationApplyType,
    val state: OperationState = OperationState.Loading,

    ) {
    init {
        check(incomingItems.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItem by lazy { incomingItems.first() }

}

enum class OperationApplyType {
    Add, Change, Clear;
}

enum class OperationRequestType {
    Add, AddMultiple, Change, ReplaceBottles, Clear;
}


enum class OperationState {
    Loading,
    Success,
    Error,
}


private interface AppCart {

    val itemsState: StateFlow<Map<Long, CartItem>>

    suspend fun incrementProduct(id: Long, quantity: Int = 1)

    suspend fun decrementProduct(id: Long, quantity: Int = 1)

    suspend fun incrementProducts(itemsMap: Map<Long, Int>)

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
