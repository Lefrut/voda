package com.m.vodovoz.common.cart

import kotlinx.coroutines.flow.MutableSharedFlow


/** Задача:
 *  Элемент корзины содержит информацию о его состоянии загрузки, количество в корзине и идентификатор.
 *
 *  Обращение к операциям корзины идет через интерфейс.
 *      Операции: изменение(удаление и т.д.), добавление как одного так и нескольких эзкмемпялов корзины.
 *          Каждая операция корзины изменяет или добавляет эземпляр корзины, если в течении 350 не поступила новая операция,
 *          то экземпляр помечается как грузящийся, после чего делается запрос в зависимости от операции
 *          Корзина отправляет запроса на облачное изменение через объект, после обновления корзины
 *
 *  Корзина уведомляет клиентов о нынешнем количестве элементов корзины.
 *
 *
 *
 *
 *
 *
 * */

abstract class AbstractAppCart : CartOperations {

    data class Item(
        val isLoading: Boolean,
        val quantity: Int,
    )

    private val items: MutableSharedFlow<Map<Long, Item>> = MutableSharedFlow(1)

    protected fun doOperation(
        operation: Operation
    ) {

    }

    protected sealed class Operation {
        protected data class Add(val id: Long, val quantity: Int) : Operation()
        protected data class AddMultiple(val idsAndQuantities: Map<Long, Int>) : Operation()
        protected data class Change(val id: Long, val quantity: Int) : Operation()
        protected data class ChangeMultiple(val idsAndQuantities: Map<Long, Int>) : Operation()
    }

}


interface CartOperations {

    suspend fun addProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun addProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>

    suspend fun changeProduct(id: Long, quantity: Int): Result<Unit>

    suspend fun changeProducts(idsAndQuantities: Map<Long, Int>): Result<Unit>
}