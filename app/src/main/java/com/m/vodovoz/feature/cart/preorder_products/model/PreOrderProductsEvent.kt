package com.m.vodovoz.feature.cart.preorder_products.model

sealed interface PreOrderProductsEvent {
    data class GoToCart(val productId: Long) : PreOrderProductsEvent
    data class GoToOrdering(val coupon: String): PreOrderProductsEvent

    data object GoBack : PreOrderProductsEvent
}
