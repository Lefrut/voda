package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.CheckboxModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.product.SectionModel

data class DeliveryDateDetailsModel(
    val title: String,
    val options: List<DeliveryDateOptionModel>,
    val button: ColorfulButtonModel,
    val earlierCheckbox: CheckboxModel?,
    val timeSections: List<SectionModel<DeliveryTimeIntervalModel>>,
)
