package com.vodovoz.app.feature.payment_method.model

sealed interface PaymentMethodEvent {

    data object GoBack: PaymentMethodEvent

}