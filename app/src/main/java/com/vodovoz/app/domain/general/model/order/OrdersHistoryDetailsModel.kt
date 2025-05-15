package com.vodovoz.app.domain.general.model.order

data class OrdersHistoryDetailsModel(
    val title: String,
    val filters: List<OrderFilterModel>,
    val items: List<OrdersHistoryItemModel>
)
