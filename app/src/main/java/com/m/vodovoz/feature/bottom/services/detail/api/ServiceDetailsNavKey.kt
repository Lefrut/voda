package com.m.vodovoz.feature.bottom.services.detail.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ServiceDetailsNavKey : NavKey {
    const val NAV_NAME: String = "feature/bottom/services/detail/ServiceDetails"
}
