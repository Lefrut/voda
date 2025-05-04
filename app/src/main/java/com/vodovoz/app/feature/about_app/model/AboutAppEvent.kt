package com.vodovoz.app.feature.about_app.model

sealed class AboutAppEvent {
    data object GoBack : AboutAppEvent()
    data object Share : AboutAppEvent()
    data object RateApp: AboutAppEvent()
    data class WriteToDevelopers(val userId: Long): AboutAppEvent()
    data class GoToWebView(val url: String, val title: String): AboutAppEvent()
}