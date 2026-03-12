package com.m.vodovoz.feature.auth.reg.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RegisterNavKey : NavKey {
    const val NAV_NAME: String = "feature/auth/reg/Register"
}
