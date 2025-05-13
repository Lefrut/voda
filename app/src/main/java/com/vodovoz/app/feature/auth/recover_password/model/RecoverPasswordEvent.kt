package com.vodovoz.app.feature.auth.recover_password.model

sealed interface RecoverPasswordEvent {

    data object GoBack: RecoverPasswordEvent

    data class GoToWebView(val url: String, val title: String): RecoverPasswordEvent

}