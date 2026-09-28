package com.m.vodovoz.domain.general.respository


interface CartRepository {
    suspend fun execute(request: CartRequest): Result<Unit>
}

sealed interface CartRequest {
    sealed interface SingleItem : CartRequest {
        val id: Long
        val quantity: Int
    }

    sealed interface MultiItem : CartRequest {
        val items: Map<Long, Int>
    }

    data class AddProduct(override val id: Long, override val quantity: Int) : SingleItem
    data class UpdateProduct(override val id: Long, override val quantity: Int) : SingleItem
    data class AddProducts(override val items: Map<Long, Int>) : MultiItem
    data class ReplaceBottles(override val items: Map<Long, Int>) : MultiItem
    data object Clear : CartRequest
}
