package com.m.vodovoz.feature.all.orders.detail.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.OrderDetailsSummaryModel

@Immutable
data class OrderDetailsSummaryUi(
    val finalPriceText: String,
    val productsPriceText: String,
    val depositText: String,
    val deliveryText: String,
    val parkingText: String,
) {
    companion object {
        val Empty = OrderDetailsSummaryUi(
            "",
            "",
            "",
            "",
            ""
        )
    }
}

fun OrderDetailsSummaryModel.toUi(): OrderDetailsSummaryUi{
    return OrderDetailsSummaryUi(
        finalPriceText, productsPriceText, depositText, deliveryText, parkingText
    )
}
