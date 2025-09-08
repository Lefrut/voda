package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.widgets.CheckboxModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.product.SectionModel

data class DeliveryDateDetailsModel(
    val title: String,
    val options: List<DeliveryDateOptionModel>,
    val button: ColorfulButtonModel,
    val earlierCheckbox: CheckboxModel?,
    val timeSections: List<SectionModel<DeliveryTimeIntervalModel>>,
)
