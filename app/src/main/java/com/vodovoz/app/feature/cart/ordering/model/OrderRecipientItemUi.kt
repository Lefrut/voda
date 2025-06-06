package com.vodovoz.app.feature.cart.ordering.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrderRecipientItemModel

@Immutable
data class OrderRecipientItemUi(
    val image: String,
    val name: String,
    val description: String,
    val id: String
)

fun OrderRecipientItemModel.toUi(): OrderRecipientItemUi{
    return OrderRecipientItemUi(image, name, description, id)
}

fun List<OrderRecipientItemModel>.mapToUi(): List<OrderRecipientItemUi>{
    return map { it.toUi() }
}
