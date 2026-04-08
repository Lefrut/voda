package com.m.vodovoz.feature.auth.login_by_phone_code.api

import androidx.compose.runtime.Immutable
import com.m.vodovoz.core.navigation.VodovozNavKey

@Immutable
data class LoginByPhoneCodeNavKey(
    val phoneNumber: String,
    val waitRequestCodeSeconds: Int,
    val userUrl: String,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_phone_code/LoginByPhoneCode"
    }
}
