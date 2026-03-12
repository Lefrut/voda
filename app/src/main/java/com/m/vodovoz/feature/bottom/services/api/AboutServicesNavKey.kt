package com.m.vodovoz.feature.bottom.services.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutServicesNavKey : NavKey {
    const val NAV_NAME: String = "feature/bottom/services/AboutServices"
}
