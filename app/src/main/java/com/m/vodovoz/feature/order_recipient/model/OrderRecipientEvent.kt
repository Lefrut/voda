package com.m.vodovoz.feature.order_recipient.model

sealed interface OrderRecipientEvent {

    data object GoBack: OrderRecipientEvent
    data object GoBackToOrdering: OrderRecipientEvent

}