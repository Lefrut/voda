package com.m.vodovoz.feature.home.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/home/Home"
}
