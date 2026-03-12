package com.m.vodovoz.feature.cancel_order.api

import androidx.navigation3.runtime.NavKey

data class CancelOrderNavKey(
    val orderId: Long,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/cancel_order/CancelOrder"
    }
}
