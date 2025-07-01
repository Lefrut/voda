package com.vodovoz.app.feature.payment_method.model

sealed interface PaymentMethodEvent {

    data object GoBack : PaymentMethodEvent
    data class GoBackToOrdering(
        val paymentMethod: PaymentMethodItemUi?,
        val paymentBalance: PaymentMethodItemUi?,
    ) : PaymentMethodEvent

}