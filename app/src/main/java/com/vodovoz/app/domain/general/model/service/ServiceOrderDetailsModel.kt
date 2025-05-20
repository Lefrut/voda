package com.vodovoz.app.domain.general.model.service

import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class ServiceOrderDetailsModel(
    val title: String,
    val subtitle: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel
)
