package com.m.vodovoz.feature.profile.change_password.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ChangePasswordNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/profile/change_password/ChangePassword"
}
