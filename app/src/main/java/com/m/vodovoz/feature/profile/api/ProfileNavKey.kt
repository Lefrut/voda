package com.m.vodovoz.feature.profile.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object ProfileNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/profile/Profile"
}
