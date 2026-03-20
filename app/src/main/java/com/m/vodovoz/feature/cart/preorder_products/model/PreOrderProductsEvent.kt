package com.m.vodovoz.feature.cart.preorder_products.model

import com.m.vodovoz.feature.cart.model.CartPresentItemUi

sealed interface PreOrderProductsEvent {
    data class GoToOrdering(
        val coupon: String,
        val productsInCart: List<CartPresentItemUi>
    ) : PreOrderProductsEvent

    class BackToCart(val productsInCart: List<CartPresentItemUi>): PreOrderProductsEvent
}
