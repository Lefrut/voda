package com.m.vodovoz.domain.general.model.location

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class AddAddressLabelBSModel(
    val title: String,
    val hint: String,
    val value: String,
    val button: ColorfulButtonModel
)
