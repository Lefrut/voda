package com.m.vodovoz.domain.general.model.order

data class OrdersHistoryProductModel(
    val showcaseProduct: Boolean,
    val image: String,
    val id: Long,
    val quantity: Int
)
