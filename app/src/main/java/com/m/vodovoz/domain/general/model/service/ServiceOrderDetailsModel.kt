package com.m.vodovoz.domain.general.model.service

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class ServiceOrderDetailsModel(
    val title: String,
    val subtitle: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel
)
