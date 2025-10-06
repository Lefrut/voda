package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.MENU_DTO
import com.m.vodovoz.data.vodovoz_service.model.OrderMenuDTO
import com.m.vodovoz.data.vodovoz_service.model.ZAKAZ_DTO
import com.m.vodovoz.domain.general.model.order.HomeOrderModel
import com.m.vodovoz.domain.general.model.order.OrderWithMenuModel
import com.m.vodovoz.domain.general.model.widgets.MenuItemModel
import com.m.vodovoz.domain.general.model.widgets.MenuItemTypeModel

fun OrderMenuDTO.toDomain(): OrderWithMenuModel {
    return OrderWithMenuModel(
        order = ZAKAZ?.toDomain(),
        menuItems = MENU?.mapNotNull { menuDto -> menuDto.toDomain() } ?: emptyList()

    )
}

fun ZAKAZ_DTO.toDomain(): HomeOrderModel? {
    return HomeOrderModel(
        orderId = this.IDZAKAZ ?: return null,
        title = this.ZAGALOVOK ?: "",
        description = this.OPISANIE ?: "",
        borderColorHex = BORDERCOLOR ?: "",
        price = this.PRICE ?: ""
    )
}

fun MENU_DTO.toDomain(): MenuItemModel? {
    return MenuItemModel(
        type = this.IDKLYCH?.mapToMenuItemTypeModel() ?: MenuItemTypeModel.None,
        picture = this.KARTINKA?.toVodovozUrl() ?: return null,
        title = this.TITLE ?: return null,
        description = this.OPISANIE ?: return null,
        borderColorHex = this.BORDERCOLOR ?: ""
    )
}

fun String.mapToMenuItemTypeModel(): MenuItemTypeModel {
    return MenuItemTypeModel.entries.firstOrNull { it.id == this } ?: MenuItemTypeModel.None
}