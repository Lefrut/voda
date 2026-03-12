package com.m.vodovoz.feature.all.orders.detail.traceorder.api

import androidx.navigation3.runtime.NavKey

data class TraceOrderNavKey(
    val driverId: String,
    val orderId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/all/orders/detail/traceorder/TraceOrder"
    }
}
