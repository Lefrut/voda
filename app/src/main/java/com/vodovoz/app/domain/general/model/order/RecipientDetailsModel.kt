package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class RecipientDetailsModel(
    val title: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel
)
