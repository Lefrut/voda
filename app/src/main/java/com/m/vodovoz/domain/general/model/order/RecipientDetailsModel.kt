package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.CheckboxModel

data class RecipientDetailsModel(
    val title: String,
    val fields: List<FieldModel>,
    val button: ColorfulButtonModel,
    val checkboxes: List<CheckboxModel>
)
