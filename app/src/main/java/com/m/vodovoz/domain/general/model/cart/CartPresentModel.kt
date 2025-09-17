package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class CartPresentModel(
    val id: Long,
    val title: String,
    val description: String,
    val image: String,
    val currentGift: Int,
    val leftToGift: Int,
    val button: ColorfulButtonModel?,
    val popupWindow: CartPresentPopupWindowModel?
)
