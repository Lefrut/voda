package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.PaymentTypeModel

@Immutable
data class PaymentTypeUi(
    val id: Int,
    val image: String,
    val name: String,
) {
    companion object {
        val Empty = PaymentTypeUi(0, "", "")
    }
}

fun List<PaymentTypeModel>.mapToUi(): List<PaymentTypeUi> {
    return map { it.toUi() }
}

fun PaymentTypeModel.toUi(): PaymentTypeUi {
    return PaymentTypeUi(
        id, image, name
    )
}
