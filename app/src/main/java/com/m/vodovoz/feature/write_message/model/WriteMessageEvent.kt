package com.m.vodovoz.feature.write_message.model

sealed interface WriteMessageEvent {

    data object GoBack : WriteMessageEvent
    data class ShowSnackbar(val message: String) : WriteMessageEvent
    data class GoToWebView(val url: String,val title: String) : WriteMessageEvent

}