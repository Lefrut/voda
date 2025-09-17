package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class OrderQuestionDetailsModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel
)