package com.vodovoz.app.domain.general.model

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

data class MenuItemModel(
    val type: MenuItemTypeModel,
    val picture: String,
    val title: String,
    val description: String,
    val borderColorHex: String,
)

enum class MenuItemTypeModel(val id: String) {
    History("history"), Payment("oplata"), None("")
}