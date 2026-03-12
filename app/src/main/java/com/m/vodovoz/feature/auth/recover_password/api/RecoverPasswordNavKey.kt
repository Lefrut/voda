package com.m.vodovoz.feature.auth.recover_password.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RecoverPasswordNavKey : NavKey {
    const val NAV_NAME: String = "feature/auth/recover_password/RecoverPassword"
}
