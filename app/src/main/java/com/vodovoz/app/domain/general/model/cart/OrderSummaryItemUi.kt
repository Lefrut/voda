package com.vodovoz.app.domain.general.model.cart

import androidx.compose.ui.graphics.Color
import com.vodovoz.app.util.fromHexOrUnspecified

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
