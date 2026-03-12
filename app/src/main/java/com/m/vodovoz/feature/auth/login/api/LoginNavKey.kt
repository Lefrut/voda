package com.m.vodovoz.feature.auth.login.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginNavKey : NavKey {
    const val NAV_NAME: String = "feature/auth/login/Login"
}
