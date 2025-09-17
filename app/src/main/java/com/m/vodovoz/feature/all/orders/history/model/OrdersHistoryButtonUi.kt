package com.m.vodovoz.feature.all.orders.history.model

import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.order.OrdersHistoryButtonModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified
import javax.annotation.concurrent.Immutable

@Immutable
data class OrdersHistoryButtonUi(
    val id: String,
    val name: String,
    val color: Color,
    val background: Color,
    val image: String,
    val url: String,
    val browserUrl: Boolean,
)

fun OrdersHistoryButtonModel.toUi(): OrdersHistoryButtonUi {
    return OrdersHistoryButtonUi(
        id = id,
        name = name,
        color = Color.fromHexOrUnspecified(color),
        background = Color.fromHexOrUnspecified(background),
        image = image,
        url = url,
        browserUrl = browserUrl
    )
}
