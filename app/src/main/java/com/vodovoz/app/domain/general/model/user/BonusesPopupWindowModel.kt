package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class BonusesPopupWindowModel(
    val title: String,
    val coupon: String,
    val description: String,
    val warmAboutExpiration: Boolean,
    val messageAboutExpiration: String,
    val button: ColorfulButtonModel,
    val url: String,
    val browser: Boolean,
)
