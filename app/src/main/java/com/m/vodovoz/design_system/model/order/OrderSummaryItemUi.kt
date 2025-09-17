package com.m.vodovoz.design_system.model.order

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.cart.OrderSummaryItemModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified

@Immutable
data class OrderSummaryItemUi(
    val name: String,
    val value: String,
    val color: Color,
)


fun List<OrderSummaryItemModel>.mapToUi(): List<OrderSummaryItemUi> {
    return map { it.toUi() }
}

fun OrderSummaryItemModel.toUi(): OrderSummaryItemUi {
    return OrderSummaryItemUi(
        name = name,
        value = value,
        color = Color.fromHexOrUnspecified(color)
    )
}
