package com.m.vodovoz.domain.general.model.cart

import com.m.vodovoz.domain.general.model.user.ForAdultsModel

data class CartPresentItemModel(
    val id: Long,
    val name: String,
    val image: String,
    val price: String?,
    val oldPrice: String?,
    val forAdults: ForAdultsModel?
)
