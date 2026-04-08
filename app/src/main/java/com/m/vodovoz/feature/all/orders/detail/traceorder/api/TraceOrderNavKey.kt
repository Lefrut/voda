package com.m.vodovoz.feature.all.orders.detail.traceorder.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class TraceOrderNavKey(
    val driverId: String,
    val orderId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/all/orders/detail/traceorder/TraceOrder"
    }
}
