package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.FieldPopupWindowModel

data class OrderingMenuItemModel(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val defaultValue: String?,
    val type: String,
)