package com.vodovoz.app.feature.all.orders.history.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.OrderFilterModel

@Immutable
data class OrderFilterUi(
    val id: String,
    val name: String,
) {
    companion object {
        val Empty = OrderFilterUi("", "")
    }
}

fun List<OrderFilterModel>.mapToUi(): List<OrderFilterUi> {
    return map { it.toUi() }
}

fun OrderFilterModel.toUi(): OrderFilterUi {
    return OrderFilterUi(id, name)
}
