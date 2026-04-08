package com.m.vodovoz.feature.auth.reg.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object RegisterNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/auth/reg/Register"
}
