package com.m.vodovoz.domain.general.model.location

import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.widgets.SwitchModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class AddAddressDetailsModel(
    val addressId: Long,
    val addressField: FieldModel,
    val gridFields: List<FieldModel>,
    val linearSwitches: List<SwitchModel>,
    val linearFields: List<FieldModel>,
    val button: ColorfulButtonModel,

    )