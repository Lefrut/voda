package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class PreOrderSectionModel(
    val title: String,
    val fields: List<FieldModel>,
    val colorfulButton: ColorfulButtonModel,
)