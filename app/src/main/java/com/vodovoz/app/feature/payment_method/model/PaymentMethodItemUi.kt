package com.vodovoz.app.feature.payment_method.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.model.order.PaymentMethodItemModel

@Immutable
data class PaymentMethodItemUi(
    val id: String,
    val code: String,
    val name: String,
    val image: String,
    val value: Boolean,
    val isSwitch: Boolean,
    val field: FieldUi?,
)

fun List<PaymentMethodItemModel>.mapToUi(): List<PaymentMethodItemUi> {
    return map { it.toUi() }
}

fun PaymentMethodItemModel.toUi(): PaymentMethodItemUi {
    return PaymentMethodItemUi(
        id = id,
        code = code,
        name = title,
        image = image,
        field = field?.toUi(),
        value = false,
        isSwitch = id == "schet"
    )
}
