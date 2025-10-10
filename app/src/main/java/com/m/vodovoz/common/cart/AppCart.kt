package com.m.vodovoz.common.cart

import com.m.vodovoz.common.cart.AbstractAppCart.Command
import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleGetOrThrow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/** Задача:
 * Элемент корзины содержит информацию о его состоянии загрузки, количество в корзине и идентификатор.
 *
 * Обращение к операциям корзины идет через интерфейс.
 * изменение(удаление и т.д.), добавление как одного так и нескольких эзкмемпялов корзины.
 * если в течении 350 не поступила новая операция, то экземпляр помечается в корзине как грузящийся,
 * после чего делается запрос в зависимости от операции. Корзина отправляет запроса на облачное изменение через объект,
 * после обновления корзины
 *
 * Клиент уведомляет корзину которая клиентов о нынешнем количестве элементов корзины.
 *
 * В корзине хранится история операций, операция добавляется в корзину при обращении к интерфейсу корзины.
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
 *
 *
 *
 * */

private suspend fun clientTest() {


}

private abstract class AbstractAppCart constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : CartOperations {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val updateVersion = AtomicInteger(0)
    private val currentOperationsFlow =
        MutableStateFlow<Map<Int, List<OperationInfo>>>(emptyMap())
    private val itemsFlow = MutableSharedFlow<Map<Long, Item>>(1)

    private val updatingMutex = Mutex()

    protected suspend fun updateItems(
        block: Map<Long, Item>.() -> Map<Long, Item>,
    ): Map<Long, Item> = updatingMutex.withLock {
        val items = itemsFlow.firstOrNull() ?: return emptyMap()
        val updatedItems = block(items)
        itemsFlow.emit(updatedItems)
        return updatedItems
    }

    override suspend fun addProduct(id: Long, quantity: Int): Result<Unit> {
        val item = Item.createItem(id, quantity)
        updateItems { plus(item.id to item) }

        return Result.failure(Throwable())
    }

    protected suspend fun doOperation() {

    }

    @OptIn(ExperimentalUuidApi::class)
    sealed class OperationInfo(
        val idsAndQuantities: Map<Long, Int>,
        val type: OperationType,
        val operationId: String = Uuid.random().toString(),
    ){


    }

    enum class OperationType {
        MultiAdd, Add, Change;
    }

    private val addCommand = createFlowCommand {
        idsAndQuantities.map { (id, quantity) ->
            vodovozServiceRepository.addProductToCart(id, quantity)
        }.first()
    }

    private val addMultiCommand: Command = createFlowCommand {
        vodovozServiceRepository.addMultipleProductsToCart(
            idsAndQuantities.toCartProducts().productsIdsWithQuantity
        )
    }

    private val changeCommand: Command = createFlowCommand {
        vodovozServiceRepository.addMultipleProductsToCart(
            idsAndQuantities.toCartProducts().productsIdsWithQuantity
        )
    }


    private fun createFlowCommand(
        block: suspend OperationInfo.() -> Flow<Result<String>>,
    ) = Command {
        runCatching { block(it).singleGetOrThrow() }
    }


    private val typesWithCommands = mapOf(
        OperationType.Add to addCommand,
        OperationType.Change to changeCommand,
        OperationType.MultiAdd to addMultiCommand
    )


    fun interface Command {
        suspend fun run(operation: OperationInfo): Result<Unit>
    }

    data class Item(
        val id: Long,
        val isLoading: Boolean,
        val quantity: Int,
    ) {

        companion object {
            fun createItem(id: Long, quantity: Int): Item {
                return Item(id, false, quantity)
            }

            fun createItems(idsAndQuantities: Map<Long, Int>): Map<Long, Item> {
                return idsAndQuantities.mapValues { (id, quantity) ->
                    createItem(id, quantity)
                }
            }
        }


    }


}


private interface CartOperations {

    suspend fun addProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>

    suspend fun changeProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>
}