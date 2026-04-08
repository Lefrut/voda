package com.m.vodovoz.feature.bottom.services.detail.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class ServiceDetailNavKey(
    val serviceId: Int,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/bottom/services/detail/ServiceDetail"
    }
}
