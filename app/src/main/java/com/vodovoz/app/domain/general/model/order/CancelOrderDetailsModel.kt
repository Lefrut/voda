package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class CancelOrderDetailsModel(
    val title: String,
    val description: String,
    val warningText: String,
    val checkboxesGroupId: String,
    val checkboxesNames: List<String>,
    val field: FieldModel?,
    val button: ColorfulButtonModel,
)
