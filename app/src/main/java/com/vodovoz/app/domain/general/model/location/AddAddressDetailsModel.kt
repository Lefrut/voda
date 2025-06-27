package com.vodovoz.app.domain.general.model.location

import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.SwitchModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class AddAddressDetailsModel(
    val addressId: Long,
    val addressField: FieldModel,
    val gridFields: List<FieldModel>,
    val linearSwitches: List<SwitchModel>,
    val linearFields: List<FieldModel>,
    val button: ColorfulButtonModel,

)