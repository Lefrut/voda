package com.m.vodovoz.feature.auth.login.model

sealed interface LoginByEmailEvent {
    data class GoToWebView(val url: String, val title: String) : LoginByEmailEvent
    data class GoBack(val selectedAccountTypeId: String) : LoginByEmailEvent
    data object GoToRegister : LoginByEmailEvent
    data object RefreshAll : LoginByEmailEvent
    data object GoToRecoverPassword : LoginByEmailEvent
}
