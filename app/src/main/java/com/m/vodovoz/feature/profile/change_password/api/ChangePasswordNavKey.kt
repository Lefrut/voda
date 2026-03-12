package com.m.vodovoz.feature.profile.change_password.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object ChangePasswordNavKey : NavKey {
    const val NAV_NAME: String = "feature/profile/change_password/ChangePassword"
}
