package com.m.vodovoz.feature.cart.ordering.api

import androidx.navigation3.runtime.NavKey

data class OrderingNavKey(
    val coupon: String,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/cart/ordering/Ordering"
    }
}
