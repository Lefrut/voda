package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.SectionModel

data class DeliveryDateDetailsModel(
    val title: String,
    val options: List<DeliveryDateOptionModel>,
    val button: ColorfulButtonModel,
    val timeSections: List<SectionModel<DeliveryTimeIntervalModel>>,
)
