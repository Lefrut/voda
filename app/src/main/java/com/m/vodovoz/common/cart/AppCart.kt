package com.m.vodovoz.common.cart

import com.m.vodovoz.common.cart.AbstractAppCart.Item
import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleGetOrThrow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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


/** Задача:
 * [AbstractAppCart.Item] содержит информацию о его состоянии загрузки, количество в корзине и идентификатор.
 *
 * Обращение к операциям корзины идет через интерфейс [CartOperations] (изменение, удаление и т.д...).
 *
 * [AbstractAppCart] содержит объект, который содержит историю операций и уведомляет корзину о их готовности.
 * Объект уведомляет корзину о операциях т.к. корзина подписывается.
 *
 * В соответсвии с операцией выполняется обновление локальной корзины, идет ожидание и проверка является
 * ли этот экзмепляр корзины последним, если да, то в соответсвии с операцией выполняется
 * соответсвующий метод загрузки на сервер
 * Каждый новый запрос в корзине, добавляет операцию с нышней версией обновления, групируются по версии
 * Если за 350 мл секунд не добавилась новая операция с данной версией обновления, то продукты начинают грузится
 * Если продукт грузится то при запросе к корзине операция не добавляется
 * В ходе выполнения запроса к интерфейсу корзины, с помощью операции и ее данных определяется операция к серверу,
 * а так же способ возвращение корзины в состояние без ее воздействия.
 * Операция возвращает результат, если результат неуспешный, то операция изменяет состояние на
 *
 *
 *
 * Мне нужен объект котрый складирует информацию о списке операций и
 *
 *
 * */

private suspend fun clientMethod() {

}

private abstract class AbstractAppCart(
    private val vodovozServiceRepository: VodovozServiceRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CartOperations {

    private val operationHandlers: OperationHandlerFactory = OperationHandlerOwner(
        multiAdd = OperationHandler(
            run = createOperationRunner {
                vodovozServiceRepository.addProductToCart(firstItem.id, firstItem.quantity)
            },
            cancel = createOperationCanceler {

            }
        ),
        add = OperationHandler(
            run = createOperationRunner {
                vodovozServiceRepository.updateProductInCart(firstItem.id, firstItem.quantity)
            },
            cancel = createOperationCanceler {

            }
        ),
        clear = OperationHandler(
            run = createOperationRunner {
                vodovozServiceRepository.addMultipleProductsToCart(items.format())
            },
            cancel = createOperationCanceler {

            }
        ),
        change = OperationHandler(
            run = createOperationRunner {
                vodovozServiceRepository.clearCart()
            },
            cancel = createOperationCanceler {

            }
        )
    )

    private val updatingMutex = Mutex()
    private val scope = CoroutineScope(dispatcher)
    private val version = AtomicInteger(0)

    private val _operationsByVersionFlow =
        MutableStateFlow<Map<Int, List<OperationInfo>>>(emptyMap())

    private fun getOperationsByVersion(version: Int): List<OperationInfo> {
        return _operationsByVersionFlow.value[version] ?: emptyList()
    }

    @OptIn(FlowPreview::class)
    private val operationsExecutor = _operationsByVersionFlow
        .map { getOperationsByVersion(version.get()) }
        .filter { it.isNotEmpty() }
        .distinctUntilChanged()
        .debounce(350)
        .onEach {
            updatingMutex.withLock {
                if (it != getOperationsByVersion(version.get())) {
                    version.incrementAndGet()
                } else {
                    throw CancellationException("Operations mismatch, aborting")
                }
            }
        }
        .map { operationInfos ->
            operationInfos.map { operationInfo ->
                scope.async { operationHandlers[operationInfo.type].run(operationInfo) }
            }.awaitAll()
        }
        .launchIn(scope)

    private val itemsSharedFlow = MutableSharedFlow<Map<Long, Item>>(1)

    protected suspend fun updateItems(
        block: Map<Long, Item>.() -> Map<Long, Item>,
    ): Map<Long, Item> = updatingMutex.withLock {
        val items = itemsSharedFlow.firstOrNull() ?: return emptyMap()
        val updatedItems = block(items)
        itemsSharedFlow.emit(updatedItems)
        return updatedItems
    }

    override suspend fun addProduct(
        id: Long,
        quantity: Int,
    ) = writeOperation(
        type = OperationType.Add,
        item = newItem(id, quantity)
    )

    override suspend fun addProducts(idsAndQuantities: Map<Long, Int>) = writeOperation(
        type = OperationType.MultiAdd,
        items = newItems(idsAndQuantities)
    )


    override suspend fun changeProducts(
        idsAndQuantities: Map<Long, Int>,
    ) = idsAndQuantities.forEach { (id, quantity) ->
        changeProduct(id, quantity)
    }

    override suspend fun changeProduct(id: Long, quantity: Int) = writeOperation(
        type = OperationType.Change,
        item = newItem(id, quantity)
    )

    override suspend fun clear() = writeOperation(
        type = OperationType.Clear,
        items = emptyList()
    )

    protected suspend fun writeOperation(
        type: OperationType,
        item: Item,
    ) = writeOperation(type = type, items = listOf(item))

    protected suspend fun writeOperation(
        type: OperationType,
        items: List<Item>,
    ) {
        if (items.isEmpty()) return
        val operation = OperationInfo(items, type)

        updatingMutex.withLock {
            val currentVersion = version.get()
            val updatedOperations = getOperationsByVersion(currentVersion).plus(operation)
            _operationsByVersionFlow.update {
                it.mapValues { (version, operations) ->
                    if (currentVersion == version) updatedOperations
                    else operations
                }
            }
        }
    }

    data class Item(
        val id: Long,
        val isLoading: Boolean,
        val quantity: Int,
    )

    fun newItem(id: Long, quantity: Int): Item {
        return Item(id, false, quantity)
    }

    fun newItems(idsAndQuantities: Map<Long, Int>): List<Item> {
        return idsAndQuantities.map { (id, quantity) ->
            newItem(id, quantity)
        }
    }

}

private fun createOperationRunner(
    block: suspend OperationInfo.() -> Flow<Result<String>>,
) = OperationRunner { operationInfo ->
    runCatching { block(operationInfo).singleGetOrThrow() }
}

private fun createOperationCanceler(
    block: suspend OperationInfo.() -> Unit,
) = OperationCanceler { operationInfo ->
    runCatching { block(operationInfo) }
}


private interface OperationHandlerFactory {

    operator fun get(operationType: OperationType): OperationHandler

}

private class OperationHandlerOwner(
    private val multiAdd: OperationHandler,
    private val add: OperationHandler,
    private val clear: OperationHandler,
    private val change: OperationHandler,
) : OperationHandlerFactory {

    override fun get(operationType: OperationType): OperationHandler {
        return when (operationType) {
            OperationType.MultiAdd -> multiAdd
            OperationType.Add -> add
            OperationType.Change -> change
            OperationType.Clear -> clear
        }
    }

}


private class OperationHandler(
    run: OperationRunner = OperationRunner { runCatching {} },
    cancel: OperationCanceler = OperationCanceler { },
) : OperationRunner by run, OperationCanceler by cancel

private fun interface OperationRunner {
    suspend fun run(operation: OperationInfo): Result<Unit>
}

private fun interface OperationCanceler {
    suspend fun cancel(operation: OperationInfo)
}

private fun List<Item>.format(): String {
    return associate { it.id to it.quantity }.toCartProducts().productsIdsWithQuantity
}

private data class OperationInfo(
    val items: List<Item>,
    val type: OperationType,
    val id: String = UUID.randomUUID().toString(),
    val state: OperationState = OperationState.Loading,
) {
    init {
        check(items.isNotEmpty()) { "OperationInfo items can't be empty" }
    }

    val firstItem by lazy { items.first() }
}

enum class OperationType {
    MultiAdd, Add, Change, Clear;
}

enum class OperationState {
    Loading,
    Success,
    Canceled;
}


private interface CartOperations {

    suspend fun addProduct(id: Long, quantity: Int)

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>)

    suspend fun changeProduct(id: Long, quantity: Int)

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>)

    suspend fun clear()
}

