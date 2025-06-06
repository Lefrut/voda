package com.vodovoz.app.domain.general.model.cart

data class OrderSummaryItemModel(
    val name: String,
    val color: String = "",
    val value: String
)
