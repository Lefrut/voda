package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.cart.OrderSummaryItemModel

data class OrderingDetailsModel(
    val title: String,
    val commentField: FieldModel?,
    val recipientSection: SectionModel<OrderingMenuItemModel>,
    val notifySection: SectionModel<OrderNotifyItemModel>,
    val paymentSection: SectionModel<OrderingMenuItemModel>,
    val totals: List<OrderSummaryItemModel>,
    val button: ColorfulButtonModel
)
