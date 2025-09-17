package com.m.vodovoz.feature.block_app.model

sealed interface BlockAppEvent {
    data object GoBack: BlockAppEvent
    data class DialPhoneNumber(val phoneNumber: String) : BlockAppEvent
    data class OpenUrl(val url: String) : BlockAppEvent
}