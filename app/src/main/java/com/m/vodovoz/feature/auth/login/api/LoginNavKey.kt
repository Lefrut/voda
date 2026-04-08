package com.m.vodovoz.feature.auth.login.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class LoginNavKey(
    val accountTypeId: String? = null,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/auth/login/Login"
    }
}
