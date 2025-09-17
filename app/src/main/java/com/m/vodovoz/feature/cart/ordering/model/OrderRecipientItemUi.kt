package com.m.vodovoz.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.OrderRecipientItemModel

@Immutable
data class OrderRecipientItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String,
    val error: Boolean = false
)

fun OrderRecipientItemModel.toUi(): OrderRecipientItemUi{
    return OrderRecipientItemUi(image, name, description, id)
}

fun List<OrderRecipientItemModel>.mapToUi(): List<OrderRecipientItemUi>{
    return map { it.toUi() }
}
