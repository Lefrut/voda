package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class PaymentMethodItemModel(
    val title: String,
    val image: String,
    val code: String,
    val id: String,
    val field: FieldModel?,
    val maxFieldValue: Int?,
    val description: String,
) {

    companion object {

        const val BONUSES_ID = "bonus"
        const val BALANCE_ID = "schet"
    }
}
