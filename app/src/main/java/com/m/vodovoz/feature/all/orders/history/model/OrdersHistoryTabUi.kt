package com.m.vodovoz.feature.all.orders.history.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.order.OrdersHistoryTabModel

@Immutable
data class OrdersHistoryTabUi(
    val id: String,
    val name: String,
    val years: List<String>,
    val selectedYear: String?,
    val placeholder: VodovozPlaceholderUi?,
)

fun List<OrdersHistoryTabModel>.mapToUi(): List<OrdersHistoryTabUi> {
    return map { it.toUi() }
}

fun OrdersHistoryTabModel.toUi(): OrdersHistoryTabUi {
    return OrdersHistoryTabUi(
        id = id,
        name = name,
        years = years,
        selectedYear = selectedYear,
        placeholder = placeholder?.toUi()
    )
}
