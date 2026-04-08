package com.m.vodovoz.feature.profile.waterapp.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object WaterAppNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/profile/waterapp/WaterApp"
    const val DEEP_LINK_PATH: String = "kalkulyator_vody"
}
