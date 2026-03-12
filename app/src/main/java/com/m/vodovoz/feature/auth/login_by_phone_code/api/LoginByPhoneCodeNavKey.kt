package com.m.vodovoz.feature.auth.login_by_phone_code.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginByPhoneCodeNavKey : NavKey {
    const val NAV_NAME: String = "feature/auth/login_by_phone_code/LoginByPhoneCode"
}
