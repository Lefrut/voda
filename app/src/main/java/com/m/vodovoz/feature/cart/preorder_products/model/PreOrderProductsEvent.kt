package com.m.vodovoz.feature.cart.preorder_products.model

import com.m.vodovoz.feature.cart.model.CartPresentItemUi

sealed interface PreOrderProductsEvent {
    data class GoToCart(val products: List<CartPresentItemUi>) : PreOrderProductsEvent
    data class GoToOrdering(
        val coupon: String
    ) : PreOrderProductsEvent

    data object GoBack : PreOrderProductsEvent
}
