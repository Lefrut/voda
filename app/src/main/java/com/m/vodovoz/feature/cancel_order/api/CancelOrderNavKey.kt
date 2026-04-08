package com.m.vodovoz.feature.cancel_order.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class CancelOrderNavKey(
    val orderId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/cancel_order/CancelOrder"
    }
}
