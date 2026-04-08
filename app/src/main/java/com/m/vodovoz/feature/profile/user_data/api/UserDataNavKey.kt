package com.m.vodovoz.feature.profile.user_data.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object UserDataNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/profile/user_data/UserData"
}
