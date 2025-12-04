package com.m.vodovoz.feature.payment_method.model

sealed interface PaymentMethodEvent {

    data object GoBack : PaymentMethodEvent
    data class GoBackToOrdering(
        val paymentMethod: PaymentMethodItemUi?,
        val paymentBalance: PaymentMethodItemUi?,
        val paymentBonuses: PaymentMethodItemUi?,
    ) : PaymentMethodEvent

}