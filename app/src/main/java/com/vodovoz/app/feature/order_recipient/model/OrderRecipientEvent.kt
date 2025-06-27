package com.vodovoz.app.feature.order_recipient.model

sealed interface OrderRecipientEvent {

    data object GoBack: OrderRecipientEvent

}