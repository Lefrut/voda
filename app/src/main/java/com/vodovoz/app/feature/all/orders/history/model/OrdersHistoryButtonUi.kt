package com.vodovoz.app.feature.all.orders.history.model

import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.order.OrdersHistoryButtonModel
import com.vodovoz.app.util.fromHexOrUnspecified
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
