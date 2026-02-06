package com.m.vodovoz.domain.general.model.widgets

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class FieldPopupWindowModel(
    val title: String,
    val description: String,
    val field: FieldModel,
    val button: ColorfulButtonModel
)