package com.vodovoz.app.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.RecipientModel

@Immutable
data class RecipientUi(
    val phone: String,
    val fio: String,
)


fun RecipientModel.toUi(): RecipientUi {
    return RecipientUi(phone, fio)
}