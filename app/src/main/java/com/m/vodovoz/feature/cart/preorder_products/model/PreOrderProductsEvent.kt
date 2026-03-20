package com.m.vodovoz.feature.cart.preorder_products.model

sealed interface PreOrderProductsEvent {
    data class GoToOrdering(
        val coupon: String,
        val productIds: List<Long>
    ) : PreOrderProductsEvent

    data class BackToCart(val productIds: List<Long>) : PreOrderProductsEvent
}
