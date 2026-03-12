package com.m.vodovoz.feature.all.orders.detail.traceorder.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TraceOrderNavKey : NavKey {
    const val NAV_NAME: String = "feature/all/orders/detail/traceorder/TraceOrder"
}
