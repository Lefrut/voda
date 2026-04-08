package com.m.vodovoz.feature.profile.waterapp.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WaterAppNavKey : NavKey {
    const val NAV_NAME: String = "feature/profile/waterapp/WaterApp"
    const val DEEP_LINK_PATH: String = "kalkulyator_vody"
}
