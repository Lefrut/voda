package com.m.vodovoz.common.cart

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
 *
 *
 *
 *
 *
 * */

private suspend fun clientTest() {

    val cart = object : AbstractAppCart() {

        override suspend fun addProduct(id: Long, quantity: Int): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun addProducts(idsAndQuantities: Map<Long, Int>): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun changeProduct(id: Long, quantity: Int): Result<Unit> {
            TODO("Not yet implemented")
        }

        override suspend fun changeProducts(idsAndQuantities: Map<Long, Int>): Result<Unit> {
            TODO("Not yet implemented")
        }

    }

    cart.addProduct(2, 10)

}

private abstract class AbstractAppCart : CartOperations {

    data class Item(
        val id: Long,
        val isLoading: Boolean,
        val quantity: Int,
    )

    private val currentOperationsFlow: MutableStateFlow<List<Operation>> =
        MutableStateFlow(emptyList())
    private val currentOperations: List<Operation> get() = currentOperationsFlow.value
    private val items: MutableSharedFlow<Map<Long, Item>> = MutableSharedFlow(1)

    override suspend fun addProduct(id: Long, quantity: Int): Result<Unit> {
        TODO("Not yet implemented")
    }

    protected suspend fun doOperation(
        operation: Operation,
    ) {
        currentOperationsFlow.emit(currentOperations + operation)



        currentOperationsFlow.emit(currentOperations - operation)
    }

    protected sealed class Operation{
        abstract val operationId: String
        abstract val idsAndQuantities: Map<Long, Int>

        @OptIn(ExperimentalUuidApi::class)
        protected data class Add(
            override val idsAndQuantities: Map<Long, Int>,
            override val operationId: String = Uuid.random().toString(),
        ) : Operation()

        @OptIn(ExperimentalUuidApi::class)
        protected data class Change(
            override val idsAndQuantities: Map<Long, Int>,
            override val operationId: String = Uuid.random().toString()
        ) : Operation()
    }

}


private interface CartOperations {

    suspend fun addProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>

    suspend fun changeProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>
}