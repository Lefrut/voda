package com.m.vodovoz.feature.service_order.api

import androidx.navigation3.runtime.NavKey

data class ServiceOrderNavKey(
    val serviceType: String,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/service_order/ServiceOrder"
    }
}
