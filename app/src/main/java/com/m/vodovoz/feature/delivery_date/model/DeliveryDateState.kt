package com.m.vodovoz.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import java.time.LocalDate

@Immutable
data class DeliveryDateState(
    val title: String = "",
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val options: List<DeliveryDateOptionUi> = emptyList(),
    val selectedDateOption: DeliveryDateOptionUi = DeliveryDateOptionUi.Empty.copy(
        value = VodovozDateFormatters.DMY.format(LocalDate.now().plusDays(1))
    ),
    val timeSections: List<SectionUi<DeliveryTimeIntervalUi>> = emptyList(),
    val selectedTimeSection: SectionUi<DeliveryTimeIntervalUi> = SectionUi.empty(),
    val selectedTimeInterval: DeliveryTimeIntervalUi = DeliveryTimeIntervalUi.Empty,
    val uiState: DeliveryDateUiState = DeliveryDateUiState.Loading,
    val earlierCheckbox: CheckboxUi? = null,
    val showCalendarDialog: Boolean = false,
)
