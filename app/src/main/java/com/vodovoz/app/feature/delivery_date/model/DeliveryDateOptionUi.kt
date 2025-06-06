package com.vodovoz.app.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.DeliveryDateOptionModel

@Immutable
data class DeliveryDateOptionUi(
    val code: String,
    val name: String,
    val value: String,
) {
    companion object {
        val Empty = DeliveryDateOptionUi("", "", "")
    }
}


fun List<DeliveryDateOptionModel>.mapToUi(): List<DeliveryDateOptionUi> {
    return mapNotNull { it.toUi() }
}

fun DeliveryDateOptionModel.toUi(): DeliveryDateOptionUi {
    return DeliveryDateOptionUi(
        code = code,
        name = name,
        value = value
    )
}
