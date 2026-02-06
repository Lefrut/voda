package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.domain.general.model.order.OrderingMenuItemModel

@Immutable
data class OrderingMenuItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val error: Boolean = false,
    val value: String?,
)

fun SectionUi<OrderingMenuItemUi>.updateItemsByIds(
    itemsIdsToTransform: List<String>,
    resetUntouchedErrors: Boolean,
    transform: (OrderingMenuItemUi) -> OrderingMenuItemUi,
): SectionUi<OrderingMenuItemUi> {
    val updatedItems = items.map { item ->
        if (itemsIdsToTransform.contains(item.id)) transform(item) else item.copy(error = if (resetUntouchedErrors) false else item.error)
    }
    return copy(items = updatedItems)
}


fun SectionUi<OrderingMenuItemUi>.clearItemErrors(): SectionUi<OrderingMenuItemUi> =
    copy(items = items.map { it.copy(error = false) })


fun OrderingMenuItemModel.toUi(): OrderingMenuItemUi {
    return OrderingMenuItemUi(
        image = image,
        name = name,
        description = description,
        id = id,
        value = defaultValue
    )
}

fun List<OrderingMenuItemModel>.mapToUi(): List<OrderingMenuItemUi> {
    return map { it.toUi() }
}

