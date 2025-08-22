package com.vodovoz.app.domain.general.model.widgets

data class MenuItemModel(
    val type: MenuItemTypeModel,
    val picture: String,
    val title: String,
    val description: String,
    val borderColorHex: String,
)

enum class MenuItemTypeModel(val id: String) {
    History("history"), Payment("oplata"), Delivery("dostavka"), None("")
}