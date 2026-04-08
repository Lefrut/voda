package com.m.vodovoz.feature.auth.recover_password.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object RecoverPasswordNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/auth/recover_password/RecoverPassword"
}
