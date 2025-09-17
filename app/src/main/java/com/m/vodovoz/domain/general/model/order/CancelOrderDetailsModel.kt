package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class CancelOrderDetailsModel(
    val title: String,
    val description: String,
    val warningText: String,
    val checkboxesGroupId: String,
    val checkboxesNames: List<String>,
    val field: FieldModel?,
    val button: ColorfulButtonModel,
)
