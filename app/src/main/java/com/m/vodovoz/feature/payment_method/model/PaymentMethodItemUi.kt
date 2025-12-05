package com.m.vodovoz.feature.payment_method.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel.Companion.BALANCE_ID
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel.Companion.BONUSES_ID
import com.m.vodovoz.util.toRoundIntOrNull
import kotlinx.parcelize.Parcelize

@Immutable
data class PaymentMethodItemUi(
    val id: String,
    val code: String,
    val name: String,
    val image: String,
    val description: String,
    val value: Boolean,
    val isSwitch: Boolean,
    val maxFieldValue: Int?,
    val field: FieldUi?,
)

val PaymentMethodItemUi.intFieldValueOrZero: Int
    get() = field?.value?.toRoundIntOrNull() ?: 0

val PaymentMethodItemUi.noDigitsFieldValue: String
    get() = field?.value?.filter { c -> c.isDigit() } ?: ""


@Parcelize
@Immutable
data class PaymentMethodItemNav(
    val id: String,
    val code: String,
    val name: String,
    val image: String,
    val description: String,
    val value: Boolean,
    val isSwitch: Boolean,
    val filedId: String,
    val fieldValue: String,
    val maxFieldValue: Int?,
) : Parcelable


fun PaymentMethodItemNav.toUi(): PaymentMethodItemUi {
    return PaymentMethodItemUi(
        id = id,
        code = code,
        name = name,
        image = image,
        value = value,
        isSwitch = isSwitch,
        field = FieldUi.Empty.copy(id = filedId, value = fieldValue),
        maxFieldValue = maxFieldValue,
        description = description
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
        fieldValue = field?.value.orEmpty(),
        maxFieldValue = maxFieldValue,
        description = description
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
        isSwitch = id == BALANCE_ID || id == BONUSES_ID,
        maxFieldValue = maxFieldValue,
        description = description
    )
}
