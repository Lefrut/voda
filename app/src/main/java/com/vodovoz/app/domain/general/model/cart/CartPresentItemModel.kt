package com.vodovoz.app.domain.general.model.cart

data class CartPresentItemModel(
    val id: Long,
    val name: String,
    val image: String,
    val price: String?,
    val oldPrice: String?
)
