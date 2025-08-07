package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.CheckBoxModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel

data class AuthDetailsModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val agreementChecked: Boolean,
    val buttons: List<ColorfulButtonModel>,
    val checkboxes: List<CheckBoxModel>
)