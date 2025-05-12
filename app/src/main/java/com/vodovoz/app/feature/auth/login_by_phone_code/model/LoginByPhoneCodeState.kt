package com.vodovoz.app.feature.auth.login_by_phone_code.model

import androidx.compose.runtime.Immutable

@Immutable
data class LoginByPhoneCodeState(
    val code: String = "",
    val phone: String = "",
    val waitSecondsText: String = "00:00",
    val requestCodeLoading: Boolean = false,
    val canRequestCode: Boolean = true,
    val blockScreen: Boolean = false,
)