package com.vodovoz.app.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import java.time.LocalDate

@Immutable
data class DeliveryDateState(
    val title: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val options: List<DeliveryDateOptionUi> = emptyList(),
    val selectedDateOption: DeliveryDateOptionUi = DeliveryDateOptionUi.Empty.copy(
        value = VodovozDateFormatters.DMY.format(LocalDate.now())
    ),
    val timeSections: List<SectionUi<DeliveryTimeIntervalUi>> = emptyList(),
    val selectedTimeSection: SectionUi<DeliveryTimeIntervalUi> = SectionUi.empty(),
    val selectedTimeInterval: DeliveryTimeIntervalUi = DeliveryTimeIntervalUi.Empty,
    val uiState: DeliveryDateUiState = DeliveryDateUiState.Loading,
    val earlierCheckbox: CheckboxUi? = null,
    val showCalendarDialog: Boolean = false,
)
