package com.m.vodovoz.feature.auth.login_by_phone_code.api

import androidx.navigation3.runtime.NavKey

data class LoginByPhoneCodeNavKey(
    val phoneNumber: String,
    val waitRequestCodeSeconds: Int,
    val user_url: String,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_phone_code/LoginByPhoneCode"
    }
}
