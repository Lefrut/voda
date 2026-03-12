package com.m.vodovoz.feature.home.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey {
    const val NAV_NAME: String = "feature/home/Home"
}
