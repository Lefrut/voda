package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class CartPresentPopupWindowModel(
    val title: String,
    val description: String,
    val items: List<CartPresentItemModel>,
    val button: ColorfulButtonModel,
    val present: CartPresentModel?
)
