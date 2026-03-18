package com.m.vodovoz.domain.general.model.cart

data class CartDetailsModel(
    val title: String,
    val countText: String,
    val items: List<CartItemModel>,
    val orderSummary: List<OrderSummaryItemModel>,
    val present: CartPresentModel?,
    val preOrderProductsPopupWindow: CartPresentPopupWindowModel?,
    val bottlesButton: CartButtonModel?,
    val promotionalCodeButton: CartPromoButtonModel?,
    val presentButton: CartButtonModel?,
)
