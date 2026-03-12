package com.m.vodovoz.feature.auth.login.api

import androidx.navigation3.runtime.NavKey

data class LoginNavKey(
    val accountTypeId: String? = null,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login/Login"
    }
}
