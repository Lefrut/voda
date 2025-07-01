package com.vodovoz.app.feature.delivery_date.model

sealed interface DeliveryDateEvent {
    data class GoBackToOrdering(
        val timeInterval: DeliveryTimeIntervalUi,
        val dateOption: DeliveryDateOptionUi,
    ) : DeliveryDateEvent

    data object GoBack : DeliveryDateEvent

}