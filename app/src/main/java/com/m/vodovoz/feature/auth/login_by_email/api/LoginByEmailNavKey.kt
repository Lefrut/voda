package com.m.vodovoz.feature.auth.login_by_email.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginByEmailNavKey : NavKey {
    const val NAV_NAME: String = "feature/auth/login_by_email/LoginByEmail"
}
