package com.m.vodovoz.feature.delivery_date.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.DeliveryTimeIntervalModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class DeliveryTimeIntervalUi(
    val name: String,
    val value: String,
    val code: String,
    val blocked: Boolean,
    val priceText: String,
): Parcelable {
    companion object{
        val Empty = DeliveryTimeIntervalUi("", "", "" , false, "")
    }
}

fun List<DeliveryTimeIntervalModel>.mapToUi(): List<DeliveryTimeIntervalUi> {
    return map { it.toUi() }
}

fun DeliveryTimeIntervalModel.toUi(): DeliveryTimeIntervalUi {
    return DeliveryTimeIntervalUi(
        name, value, code, blocked, priceText
    )
}
