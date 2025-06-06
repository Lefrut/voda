package com.vodovoz.app.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi

@Immutable
data class DeliveryDateState(
    val title: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val options: List<DeliveryDateOptionUi> = emptyList(),
    val selectedOption: DeliveryDateOptionUi = DeliveryDateOptionUi.Empty,
    val timeSections: List<SectionUi<DeliveryTimeIntervalUi>> = emptyList(),
    val selectedTimeSection: SectionUi<DeliveryTimeIntervalUi> = SectionUi.empty(),
    val uiState: DeliveryDateUiState = DeliveryDateUiState.Loading
)
