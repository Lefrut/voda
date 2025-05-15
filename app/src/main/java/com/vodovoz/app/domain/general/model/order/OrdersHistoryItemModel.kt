package com.vodovoz.app.domain.general.model.order

import java.time.LocalDate

data class OrdersHistoryItemModel(
    val id: Long,
    val priceText: String,
    val address: String,
    val description: String,
    val status: OrderStatusModel?,
    val date: LocalDate?,
    val products: List<OrdersHistoryProductModel>,
    val button: OrdersHistoryButtonModel?
)
