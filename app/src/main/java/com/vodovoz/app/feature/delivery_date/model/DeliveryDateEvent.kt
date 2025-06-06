package com.vodovoz.app.feature.delivery_date.model

sealed interface DeliveryDateEvent {

    data object GoBack: DeliveryDateEvent

}