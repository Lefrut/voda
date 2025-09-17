package com.m.vodovoz.feature.cancel_order.model

sealed class CancelOrderEvent{

    data object GoBack: CancelOrderEvent()

}
