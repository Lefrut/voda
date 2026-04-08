package com.m.vodovoz.feature.all.orders.detail.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class OrderDetailsNavKey(
    val orderId: Long,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/all/orders/detail/OrderDetails"
    }
}
