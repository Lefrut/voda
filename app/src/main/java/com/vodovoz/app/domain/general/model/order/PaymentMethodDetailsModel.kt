package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.SectionModel

data class PaymentMethodDetailsModel(
    val title: String,
    val items: List<SectionModel<PaymentMethodItemModel>>,
    val button: ColorfulButtonModel,
)
