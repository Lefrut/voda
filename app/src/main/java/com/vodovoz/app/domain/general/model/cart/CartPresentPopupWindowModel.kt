package com.vodovoz.app.domain.general.model.cart

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class CartPresentPopupWindowModel(
    val items: List<CartPresentItemModel>,
    val button: ColorfulButtonModel
)
