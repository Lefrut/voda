package com.m.vodovoz.feature.order_call_you.api

import androidx.navigation3.runtime.NavKey

data class OrderCallYouNavKey(
    val addressId: Long,
    val callYouId: String? = null,
    val queryParams: Map<String, String> = emptyMap(),
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/order_call_you/OrderCallYou"
    }
}
