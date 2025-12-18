package com.m.vodovoz.common.cart

import android.R
import com.m.vodovoz.domain.general.model.product.toCartProducts
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

open class AbstractAppCart(
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

    protected suspend fun updateItems(
        block: Map<Long, CartItem>.() -> Map<Long, CartItem>,
    ): Map<Long, CartItem> = updatingMutex.withLock {
        val updatedItems = block(items)
        _itemsState.value = updatedItems
        return updatedItems
    }

    private val _operationsFlow =
        MutableStateFlow<List<OperationInfo>>(emptyList())


    @OptIn(FlowPreview::class)
    private val operationsExecutor = _operationsFlow
        .filter { infos -> infos.isNotEmpty() }
        .distinctUntilChanged()
        .debounce { 375.milliseconds }
        .onEach { operationInfos ->

        }
        .launchIn(scope)


    private var itemsSnaphot: Map<Long, CartItem>? = null

    override fun incrementProduct(id: Long, quantity: Int): Job = scope.launch {
        if (itemsSnaphot == null) itemsSnaphot = items.toMap()

        val type = if (itemsSnaphot?.get(id) != null) {
            OperationType.Change
        } else {
            OperationType.Add
        }

        val operationInfo = OperationInfo(
            items = listOf(CartItem.from(id, quantity)).associateBy { item ->
                item.id
            },
            type = type
        )

        val mutableOperationsList = _operationsFlow.value.toMutableList()
        type.merge(
            queue = mutableOperationsList,
            incoming = operationInfo
        )

        when (startPolicy) {
            AppCart.StartPolicy.QUEUED -> {
                _operationsFlow.value = mutableOperationsList
            }

            AppCart.StartPolicy.IMMEDIATE -> {

            }
        }


        /**
         * ## Операции
         * Удаление или изменение предыдущих операций, которые имеют содержат
         * те же товары, что и новая операция. Операция может изменять или добавлять
         * один или более товаров, зависит от наличия в локальной корзине. Если новая
         * операция добавляет товар, который уже в очереди, то суммирует к нынешнему кол-ву или
         * отнимает если опреция убавляет продукт или просто удаляет если операци ничего не делает.
         * Если изменяет, то все предыдущии товары на изменение удаляются вне зависимости от операции.
         *
         * Иначе говоря, действие операции, которое знает о нынешней операции и выполняет преобразование нынешних
         * операций, которые содержат те же товары (обязательно, не должно быть дубликатов)
         * и над всеми товарами по своим критериям.
         *
         *
         * Алгоритм "объеденения" товаров:
         * 1.
         * 2.
         * 3.
         *
         * */

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

        fun from(idsAndQuantities: Map<Long, Int>): List<CartItem> {
            return idsAndQuantities.map { (id, quantity) ->
                from(id, quantity)
            }
        }
    }
}


private fun List<CartItem>.format(): String {
    return associate { it.id to it.quantity }.toCartProducts().productsIdsWithQuantity
}

data class OperationInfo(
    val items: Map<Long, CartItem>,
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val state: OperationState = OperationState.Loading,

    ) {
    init {
        check(items.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItem by lazy { items.getValue(0) }

}


sealed interface OperationType {

    fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo)


    data object Add : OperationType {
        override fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
            queue.mergeDeltaLike(incoming) { oldQ, incQ -> oldQ + incQ }
        }
    }

    data object Change : OperationType {
        override fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
            val ids = incoming.items.keys
            queue.dropIdsFromTail(ids)
            val idx = queue.lastIndexFromTailUntilClear { it.type == Change }
            if (idx != null) {
                val op = queue[idx]
                queue[idx] = op.copy(items = op.items + incoming.items)
            } else {
                queue.add(incoming)
            }
        }
    }

    data object Clear : OperationType {
        override fun merge(queue: MutableList<OperationInfo>, incoming: OperationInfo) {
            queue.clear()
            queue.add(incoming)
        }
    }

}


private fun MutableList<OperationInfo>.dropIdsFromTail(ids: Set<Long>) {
    reversed().forEachIndexed { i, op ->
        val newItems = op.items - ids
        if (newItems.size != op.items.size) setOrRemove(i, op, newItems)
    }
}

private inline fun List<OperationInfo>.lastIndexFromTailUntilClear(
    predicate: (OperationInfo) -> Boolean
): Int? {
    for (i in indices.reversed()) {
        val op = this[i]
        if (op.type == OperationType.Clear) break
        if (predicate(op)) return i
    }
    return null
}

private fun MutableList<OperationInfo>.mergeDeltaLike(
    incoming: OperationInfo,
    qtyMerge: (old: Int, inc: Int) -> Int
) {
    val inc = incoming.items.toMutableMap()

    reversed().forEachIndexed { i, op ->
        if (inc.isEmpty()) return

        // берём только те id, которые реально есть в op
        val touchedIds = inc.keys.filter { it in op.items }
        if (touchedIds.isEmpty()) return@forEachIndexed

        val newItems = op.items.toMutableMap()

        for (id in touchedIds) {
            val oldItem = newItems.getValue(id)
            val incItem = inc.getValue(id)

            val q = qtyMerge(oldItem.quantity, incItem.quantity)
            if (q == 0) newItems.remove(id) else newItems[id] = oldItem.copy(quantity = q)

            inc.remove(id)
        }

        setOrRemove(i, op, newItems)
    }

    if (inc.isNotEmpty()) add(incoming.copy(items = inc))
}

private fun MutableList<OperationInfo>.setOrRemove(
    index: Int,
    op: OperationInfo,
    newItems: Map<Long, CartItem>
) {
    if (newItems.isEmpty()) removeAt(index) else set(index, op.copy(items = newItems))
}

enum class OperationRequestType {
    Add, AddMultiple, Change, ReplaceBottles, Clear;
}


enum class OperationState {
    Loading,
    Success,
    Error,
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
