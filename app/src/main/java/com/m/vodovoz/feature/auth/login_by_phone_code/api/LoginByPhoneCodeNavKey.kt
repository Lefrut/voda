package com.m.vodovoz.feature.auth.login_by_phone_code.api

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey

@Immutable
data class LoginByPhoneCodeNavKey(
    val phoneNumber: String,
    val waitRequestCodeSeconds: Int,
    val userUrl: String,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_phone_code/LoginByPhoneCode"
    }
}
