package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.widgets.CheckboxModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.widgets.FieldModel

data class AuthDetailsModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val agreementChecked: Boolean?,
    val buttons: List<ColorfulButtonModel>,
    val checkboxes: List<CheckboxModel>
)