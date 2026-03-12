package com.m.vodovoz.feature.profile.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProfileNavKey : NavKey {
    const val NAV_NAME: String = "feature/profile/Profile"
}
