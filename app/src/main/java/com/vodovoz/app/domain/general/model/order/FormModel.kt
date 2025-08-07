package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class FormModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel,
)