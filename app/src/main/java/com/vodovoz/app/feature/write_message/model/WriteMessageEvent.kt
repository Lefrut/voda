package com.vodovoz.app.feature.write_message.model

sealed interface WriteMessageEvent {

    data object GoBack : WriteMessageEvent
    data class ShowSnackbar(val message: String) : WriteMessageEvent

}