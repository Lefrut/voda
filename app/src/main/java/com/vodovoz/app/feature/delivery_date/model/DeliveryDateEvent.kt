package com.vodovoz.app.feature.delivery_date.model

import com.vodovoz.app.design_system.model.widgets.CheckboxUi

sealed interface DeliveryDateEvent {
    data class GoBackToOrdering(
        val timeInterval: DeliveryTimeIntervalUi,
        val dateOption: DeliveryDateOptionUi,
        val earlierCheckbox: CheckboxUi?
    ) : DeliveryDateEvent

    data object GoBack : DeliveryDateEvent

}