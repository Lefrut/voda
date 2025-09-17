package com.m.vodovoz.feature.all.orders.detail.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.order.OrderStatusModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified

@Immutable
data class OrderStatusUi(
    val name: String,
    val icon: String,
    val color: Color,
    val background: Color,
    val backgroundAlpha: Float
) {
    companion object {
        val Empty = OrderStatusUi("", "", Color.Unspecified, Color.Unspecified, 1f)
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
        background = Color.fromHexOrUnspecified(background),
        backgroundAlpha = backgroundAlpha
    )
}