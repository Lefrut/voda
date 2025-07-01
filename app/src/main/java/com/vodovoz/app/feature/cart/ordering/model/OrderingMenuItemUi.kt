package com.vodovoz.app.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrderingMenuItemModel

@Immutable
data class OrderingMenuItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val error: Boolean = false
)

fun OrderingMenuItemModel.toUi(): OrderingMenuItemUi{
    return OrderingMenuItemUi(image, name, description, id)
}

fun List<OrderingMenuItemModel>.mapToUi(): List<OrderingMenuItemUi>{
    return map { it.toUi() }
}

