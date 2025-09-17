package com.m.vodovoz.feature.order_call_you.model

sealed interface OrderCallYouEvent {
    data class GoBackToOrdering(val currentItem: CallYouItemUi) : OrderCallYouEvent

    data object GoBack: OrderCallYouEvent

}