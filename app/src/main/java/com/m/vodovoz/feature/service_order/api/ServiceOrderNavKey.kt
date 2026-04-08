package com.m.vodovoz.feature.service_order.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class ServiceOrderNavKey(
    val serviceType: String,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/service_order/ServiceOrder"
    }
}
