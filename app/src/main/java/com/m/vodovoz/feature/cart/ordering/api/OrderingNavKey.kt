package com.m.vodovoz.feature.cart.ordering.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class OrderingNavKey(
    val coupon: String,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/cart/ordering/Ordering"
    }
}
