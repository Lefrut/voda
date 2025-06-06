package com.vodovoz.app.domain.general.model.order

data class DeliveryTimeIntervalModel(
    val name: String,
    val value: String,
    val code: String,
    val blocked: Boolean,
    val priceText: String,
)
