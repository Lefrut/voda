package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.LabelModel

data class DeliveryTimeIntervalModel(
    val name: String,
    val value: String,
    val code: String,
    val label: LabelModel?,
    val blocked: Boolean,
    val priceText: String,
)
