package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.widgets.CheckboxModel
import com.vodovoz.app.domain.general.model.widgets.FieldModel

data class FormModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel,
    val checkbox: CheckboxModel?
)