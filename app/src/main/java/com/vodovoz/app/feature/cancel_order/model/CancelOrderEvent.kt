package com.vodovoz.app.feature.cancel_order.model

sealed class CancelOrderEvent{

    data object GoBack: CancelOrderEvent()

}
