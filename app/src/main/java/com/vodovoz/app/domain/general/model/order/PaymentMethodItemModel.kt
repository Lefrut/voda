package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.widgets.FieldModel

data class PaymentMethodItemModel(
    val title: String,
    val image: String,
    val code: String,
    val id: String,
    val field: FieldModel?
)
