package com.vodovoz.app.feature.all.orders.history.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrdersHistoryProductModel

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
