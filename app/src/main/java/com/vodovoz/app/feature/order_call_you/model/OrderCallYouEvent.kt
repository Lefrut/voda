package com.vodovoz.app.feature.order_call_you.model

sealed interface OrderCallYouEvent {

    data object GoBack: OrderCallYouEvent

}