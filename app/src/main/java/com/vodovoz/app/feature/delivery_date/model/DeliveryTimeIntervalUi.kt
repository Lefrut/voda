package com.vodovoz.app.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.DeliveryTimeIntervalModel

@Immutable
data class DeliveryTimeIntervalUi(
    val name: String,
    val value: String,
    val code: String,
    val blocked: Boolean,
    val priceText: String,
)

fun List<DeliveryTimeIntervalModel>.mapToUi(): List<DeliveryTimeIntervalUi> {
    return map { it.toUi() }
}

fun DeliveryTimeIntervalModel.toUi(): DeliveryTimeIntervalUi {
    return DeliveryTimeIntervalUi(
        name, value, code, blocked, priceText
    )
}
