package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.order.OrderNotifySectionModel

@Immutable
data class OrderNotifySectionUi(
    val title: String,
    val options: List<OrderNotifyItemUi>,
    val extraPhoneField: FieldUi?,
){
    companion object{
        val Empty = OrderNotifySectionUi("", emptyList(), null)
    }
}

fun OrderNotifySectionModel.toUi(): OrderNotifySectionUi {
    return OrderNotifySectionUi(
        title = title,
        options = notifyItems.mapToUi(),
        extraPhoneField = field?.toUi()
    )
}
