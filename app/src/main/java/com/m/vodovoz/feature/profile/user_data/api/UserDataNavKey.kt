package com.m.vodovoz.feature.profile.user_data.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object UserDataNavKey : NavKey {
    const val NAV_NAME: String = "feature/profile/user_data/UserData"
}
