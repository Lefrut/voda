package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.widgets.FieldModel

data class OrderNotifySectionModel(
    val title: String,
    val notifyItems: List<OrderNotifyItemModel>,
    val field: FieldModel?,
) {
    companion object {
        val Empty = OrderNotifySectionModel("", emptyList(), null)
    }
}
