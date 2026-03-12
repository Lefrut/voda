package com.m.vodovoz.feature.map.api

import androidx.navigation3.runtime.NavKey

data class MapNavKey(
    val addressName: String? = null,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/map/Map"
    }
}
