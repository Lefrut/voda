package com.vodovoz.app.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrderPaymentItemModel

@Immutable
data class OrderPaymentItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
)

fun OrderPaymentItemModel.toUi(): OrderPaymentItemUi{
    return OrderPaymentItemUi(image, name, description, id)
}

fun List<OrderPaymentItemModel>.mapToUi(): List<OrderPaymentItemUi>{
    return map { it.toUi() }
}

