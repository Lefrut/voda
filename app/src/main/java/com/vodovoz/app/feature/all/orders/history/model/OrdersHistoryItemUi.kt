package com.vodovoz.app.feature.all.orders.history.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrdersHistoryItemModel
import com.vodovoz.app.feature.all.orders.detail.model.OrderStatusUi
import com.vodovoz.app.feature.all.orders.detail.model.toUi

@Immutable
data class OrdersHistoryItemUi(
    val id: Long,
    val address: String,
    val description: String,
    val status: OrderStatusUi?,
    val products: List<OrdersHistoryProductUi>,
    val priceText: String,
    val button: OrdersHistoryButtonUi?,
)


fun List<OrdersHistoryItemModel>.mapToUi(): List<OrdersHistoryItemUi>{
    return map { it.toUi() }
}

fun OrdersHistoryItemModel.toUi(): OrdersHistoryItemUi{
    return OrdersHistoryItemUi(
        id = id,
        address = address,
        description = description,
        status = status?.toUi(),
        products = products.mapToUi(),
        priceText = priceText,
        button = button?.toUi()
    )
}
