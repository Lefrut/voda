package com.m.vodovoz.feature.all.orders.history.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.OrdersHistoryProductModel

@Immutable
data class OrdersHistoryProductUi(
    val showcaseProduct: Boolean,
    val image: String,
    val id: Long,
    val quantity: Int
)


fun List<OrdersHistoryProductModel>.mapToUi(): List<OrdersHistoryProductUi>{
    return map { it.toUi() }
}

fun OrdersHistoryProductModel.toUi(): OrdersHistoryProductUi{
    return OrdersHistoryProductUi(showcaseProduct, image, id, quantity)
}
