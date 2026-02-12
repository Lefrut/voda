package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.FieldPopupWindowUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.order.OrderingMenuItemModel
import com.m.vodovoz.domain.general.model.widgets.FieldPopupWindowModel


@Immutable
data class OrderingMenuItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val error: Boolean = false,
    val value: String?,
    val type: OrderingMenuItemType
)

@Stable
sealed interface OrderingMenuItemType {

    data object Default : OrderingMenuItemType
    data object Switch : OrderingMenuItemType

}

fun List<OrderingMenuItemUi>.toIdAndValueMap(): Map<String, String> {
    return associate { it.id to (it.value ?: "") }
}

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
        value = defaultValue,
        type = when (type) {
            "chekbox" -> OrderingMenuItemType.Switch
            else -> OrderingMenuItemType.Default
        }
    )
}

fun FieldPopupWindowModel.toUi(): FieldPopupWindowUi {
    return FieldPopupWindowUi(
        title = title,
        field = field.toUi(),
        description = description,
        button = button.toUi()
    )
}

fun List<OrderingMenuItemModel>.mapToUi(): List<OrderingMenuItemUi> {
    return map { it.toUi() }
}

