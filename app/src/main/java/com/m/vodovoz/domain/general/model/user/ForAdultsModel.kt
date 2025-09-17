package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class ForAdultsModel(
    val title: String,
    val description: String,
    val textBlur: String,
    val button: ColorfulButtonModel
)
