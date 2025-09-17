package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.product.SectionModel

data class PaymentMethodDetailsModel(
    val title: String,
    val items: List<SectionModel<PaymentMethodItemModel>>,
    val button: ColorfulButtonModel,
)
