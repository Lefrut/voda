package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.cart.OrderSummaryItemModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class OrderDetailsModel(
    val title: String,
    val subtitle: String,
    val header: String,
    val currentStatus: List<OrderStatusModel>,
    val statuses: List<OrderStatusModel>,
    val topButtons: List<OrderDetailsButtonModel>,
    val bottomButtons: List<ColorfulButtonModel>,
    val orderSummary: List<OrderSummaryItemModel>,
    val productsTitle: String,
    val products: List<OrderProductModel>
)
