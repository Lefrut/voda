package com.m.vodovoz.feature.auth.login_by_email.api

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
data class LoginByEmailNavKey(
    val accountTypeId: String? = null,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_email/LoginByEmail"
    }
}
