package com.m.vodovoz.feature.map.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MapNavKey : NavKey {
    const val NAV_NAME: String = "feature/map/Map"
}
