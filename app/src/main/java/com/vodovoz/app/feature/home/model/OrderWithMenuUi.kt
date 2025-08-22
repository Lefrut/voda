package com.vodovoz.app.feature.home.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.order.HomeOrderModel
import com.vodovoz.app.domain.general.model.order.OrderWithMenuModel
import com.vodovoz.app.domain.general.model.widgets.MenuItemModel
import com.vodovoz.app.domain.general.model.widgets.MenuItemTypeModel
import com.vodovoz.app.ui.graphics.fromHexOrNull

@Immutable
data class OrderWithMenuUi(
    val order: HomeOrderUi? = null,
    val menuItems: List<MenuItemUi>,
){
    companion object {
        val Empty = OrderWithMenuUi(menuItems = emptyList())
    }
}


fun OrderWithMenuModel.toUi(): OrderWithMenuUi {
    return OrderWithMenuUi(order?.toUi(), menuItems.map { it.toUi() })
}

fun HomeOrderModel.toUi(): HomeOrderUi {
    return HomeOrderUi(
        orderId = orderId,
        title = title,
        text = description,
        price = price,
        borderColor = Color.fromHexOrNull(borderColorHex)
    )
}

fun MenuItemModel.toUi(): MenuItemUi {
    return MenuItemUi(
        image = this.picture,
        title = title,
        description = description,
        type = type.toUi(),
        borderColor = Color.fromHexOrNull(borderColorHex)
    )
}



@Immutable
data class HomeOrderUi(
    val orderId: Long,
    val title: String,
    val text: String,
    val price: String,
    val borderColor: Color?,
)

@Immutable
data class MenuItemUi(
    val image: String,
    val title: String,
    val description: String,
    val type: MenuItemTypeUi,
    val borderColor: Color?,
)

enum class MenuItemTypeUi(val id: String) {
    History("history"), Payment("oplata"), Delivery("dostavka"),None("")
}

fun MenuItemTypeModel.toUi(): MenuItemTypeUi{
    return MenuItemTypeUi.entries.firstOrNull { it.id == id } ?: MenuItemTypeUi.None
}