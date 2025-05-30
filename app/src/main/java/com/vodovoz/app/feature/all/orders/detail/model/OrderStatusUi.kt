package com.vodovoz.app.feature.all.orders.detail.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.order.OrderStatusModel
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified

@Immutable
data class OrderStatusUi(
    val name: String,
    val icon: String,
    val color: Color,
    val background: Color,
) {
    companion object {
        val Empty = OrderStatusUi("", "", Color.Unspecified, Color.Unspecified)
    }
}

fun List<OrderStatusModel>.mapToUi(): List<OrderStatusUi> {
    return map { it.toUi() }
}

fun OrderStatusModel.toUi(): OrderStatusUi {
    return OrderStatusUi(
        name = name,
        icon = image,
        color = Color.fromHexOrUnspecified(color),
        background = Color.fromHexOrUnspecified(background)
    )
}