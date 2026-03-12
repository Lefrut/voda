package com.m.vodovoz.feature.all.orders.detail.api

import androidx.navigation3.runtime.NavKey

data class OrderDetailsNavKey(
    val orderId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/all/orders/detail/OrderDetails"
    }
}
