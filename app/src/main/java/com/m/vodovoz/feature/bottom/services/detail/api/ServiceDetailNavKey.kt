package com.m.vodovoz.feature.bottom.services.detail.api

import androidx.navigation3.runtime.NavKey

data class ServiceDetailNavKey(
    val serviceId: Int,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/bottom/services/detail/ServiceDetail"
    }
}
