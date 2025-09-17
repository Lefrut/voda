package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.OrderingMenuItemModel

@Immutable
data class OrderingMenuItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val error: Boolean = false,
    val defaultValue: String?,
)

fun OrderingMenuItemModel.toUi(): OrderingMenuItemUi {
    return OrderingMenuItemUi(
        image = image,
        name = name,
        description = description,
        id = id,
        defaultValue = defaultValue
    )
}

fun List<OrderingMenuItemModel>.mapToUi(): List<OrderingMenuItemUi> {
    return map { it.toUi() }
}

