package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.CheckboxModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class FormModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel,
    val checkbox: CheckboxModel?
)