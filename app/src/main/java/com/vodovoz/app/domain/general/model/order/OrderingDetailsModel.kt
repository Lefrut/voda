package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.cart.OrderSummaryItemModel

data class OrderingDetailsModel(
    val title: String,
    val commentField: FieldModel?,
    val recipientSection: SectionModel<OrderRecipientItemModel>,
    val notifySection: SectionModel<OrderNotifyItemModel>,
    val paymentSection: SectionModel<OrderPaymentItemModel>,
    val totals: List<OrderSummaryItemModel>,
    val button: ColorfulButtonModel
)
