package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.widgets.MenuItemModel

data class OrderWithMenuModel(
    val order: HomeOrderModel? = null,
    val menuItems: List<MenuItemModel>,
)

data class HomeOrderModel(
    val orderId: Long,
    val title: String,
    val description: String,
    val borderColorHex: String,
    val price: String,
)
