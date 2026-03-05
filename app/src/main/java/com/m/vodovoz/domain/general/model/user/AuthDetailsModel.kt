package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.CheckboxModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.widgets.SwitchModel

data class AuthDetailsModel(
    val title: String,
    val description: String,
    val fields: List<FieldModel>,
    val agreementCheckboxId: String?,
    val buttons: List<ColorfulButtonModel>,
    val checkboxes: List<CheckboxModel>,
    val accountTypeSection: SectionModel<SwitchModel>
)