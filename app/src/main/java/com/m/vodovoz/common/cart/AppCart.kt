package com.m.vodovoz.common.cart

import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


/** Задача:
 * Элемент корзины содержит информацию о его состоянии загрузки, количество в корзине и идентификатор.
 *
 * Обращение к операциям корзины идет через интерфейс.
 *      Операции: изменение(удаление и т.д.), добавление как одного так и нескольких эзкмемпялов корзины.
 *          Каждая операция корзины содержит информацию о количестве и типе операции,
 *          если в течении 350 не поступила новая операция, то экземпляр помечается в корзине как грузящийся,
 *          после чего делается запрос в зависимости от операции
 *          Корзина отправляет запроса на облачное изменение через объект, после обновления корзины
 *
 * Клиент уведомляет корзину которая клиентов о нынешнем количестве элементов корзины.
 *
 * В корзине хранится история операций, операция добавляется в корзину при обращении к интерфейсу корзины
 * и корректных значений передаваемого продукта.
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

    data class Item(
        val id: Long,
        val isLoading: Boolean,
        val quantity: Int,
    )

    fun createItem(id: Long, quantity: Int): Item {
        return Item(id, false, quantity)
    }

    fun createItems(idsAndQuantities: Map<Long, Int>): Map<Long, Item> {
        return idsAndQuantities.mapValues { (id, quantity) -> createItem(id, quantity) }
    }

    private val updateVersion = AtomicInteger(0)
    private val currentOperationsFlow: MutableStateFlow<Map<Int, List<Operation>>> =
        MutableStateFlow(emptyMap())
    private val itemsFlow: MutableSharedFlow<Map<Long, Item>> = MutableSharedFlow(1)

    private val updatingMutex = Mutex()

    protected suspend fun updateItems(block: Map<Long, Item>.() -> Map<Long, Item>): Map<Long, Item> =
        updatingMutex.withLock {
            val items = itemsFlow.firstOrNull() ?: return emptyMap()
            val updatedItems = block(items)
            itemsFlow.emit(updatedItems)
            return updatedItems
        }

    override suspend fun addProduct(id: Long, quantity: Int): Result<Unit> {
        val item = createItem(id, quantity)
        updateItems { plus(item.id to item) }


        TODO("Not yet implemented")
    }

    protected suspend fun doOperation(
        operation: Operation,
    ) {

        when (operation) {
            is Operation.Add -> {
                if (operation.idsAndQuantities.size > 1) {
                    //vodovozServiceRepository.addMultipleProductsToCart()
                } else {
                    //vodovozServiceRepository.addProductToCart()
                }
            }

            is Operation.Change -> {
                operation.idsAndQuantities.forEach { (id, quantity) ->
                    vodovozServiceRepository.updateProductInCart(id, quantity)
                }
            }
        }
    }

    protected sealed class Operation {
        abstract val operationId: String
        abstract val idsAndQuantities: Map<Long, Int>


        @OptIn(ExperimentalUuidApi::class)
        data class Add(
            override val idsAndQuantities: Map<Long, Int>,
            override val operationId: String = Uuid.random().toString(),
        ) : Operation()

        @OptIn(ExperimentalUuidApi::class)
        data class Change(
            override val idsAndQuantities: Map<Long, Int>,
            override val operationId: String = Uuid.random().toString(),
        ) : Operation()
    }

    private class SendCommand(

    ) {

        suspend fun send() {

        }


    }

}


private interface CartOperations {

    suspend fun addProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>

    suspend fun changeProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>
}