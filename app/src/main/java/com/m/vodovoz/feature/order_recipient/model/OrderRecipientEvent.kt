package com.m.vodovoz.feature.order_recipient.model

sealed interface OrderRecipientEvent {
    data class GoToWebView(val url: String, val title: String): OrderRecipientEvent
    data object GoBack: OrderRecipientEvent
    data object GoBackToOrdering: OrderRecipientEvent

}