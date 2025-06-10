package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.product.SectionModel

data class DeliveryDateDetailsModel(
    val title: String,
    val options: List<DeliveryDateOptionModel>,
    val button: ColorfulButtonModel,
    val timeSections: List<SectionModel<DeliveryTimeIntervalModel>>,
)
