package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class RecipientDetailsModel(
    val title: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel
)
