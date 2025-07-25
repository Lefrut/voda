package com.vodovoz.app.feature.write_message.model

sealed interface WriteMessageEvent {

    data object GoBack: WriteMessageEvent

}