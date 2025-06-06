package com.vodovoz.app.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrderNotifyItemModel
import com.vodovoz.app.domain.general.model.order.OrderPaymentItemModel


@Immutable
data class OrderNotifyItemUi(
    val name: String,
    val value: String,
    val code: String,
){
    companion object{
        val Empty = OrderNotifyItemUi("","", "")
    }
}

fun OrderNotifyItemModel.toUi(): OrderNotifyItemUi{
    return OrderNotifyItemUi(name, value, code)
}

fun List<OrderNotifyItemModel>.mapToUi(): List<OrderNotifyItemUi>{
    return map { it.toUi() }
}

