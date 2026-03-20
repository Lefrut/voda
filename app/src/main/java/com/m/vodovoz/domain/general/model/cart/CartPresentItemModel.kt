package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import com.m.vodovoz.domain.general.model.widgets.LabelModel

data class CartPresentItemModel(
    val id: Long,
    val name: String,
    val image: String,
    val price: String?,
    val oldPrice: String?,
    val label: LabelModel?,
    val maxQuantity: Int,
    val forAdults: ForAdultsModel?
)
