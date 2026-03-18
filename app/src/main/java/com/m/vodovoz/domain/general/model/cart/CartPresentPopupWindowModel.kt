package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class CartPresentPopupWindowModel(
    val purchase: Boolean,
    val items: List<CartPresentItemModel>,
    val button: ColorfulButtonModel,
    val present: CartPresentModel?
)
