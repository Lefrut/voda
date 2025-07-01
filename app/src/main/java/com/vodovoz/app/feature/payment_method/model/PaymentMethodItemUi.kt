package com.vodovoz.app.feature.payment_method.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.model.order.PaymentMethodItemModel
import kotlinx.parcelize.Parcelize

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

@Parcelize
@Immutable
data class PaymentMethodItemNav(
    val id: String,
    val code: String,
    val name: String,
    val image: String,
    val value: Boolean,
    val isSwitch: Boolean,
    val filedId: String,
    val fieldValue: String,
) : Parcelable

fun PaymentMethodItemNav.toUi(): PaymentMethodItemUi {
    return PaymentMethodItemUi(
        id = id,
        code = code,
        name = name,
        image = image,
        value = value,
        isSwitch = isSwitch,
        field = FieldUi.Empty.copy(id = filedId, value = fieldValue)
    )
}

fun PaymentMethodItemUi.toNav(): PaymentMethodItemNav {
    return PaymentMethodItemNav(
        id = id,
        code = code,
        name = name,
        image = image,
        value = value,
        isSwitch = isSwitch,
        filedId = field?.id.orEmpty(),
        fieldValue = field?.value.orEmpty()
    )
}

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
