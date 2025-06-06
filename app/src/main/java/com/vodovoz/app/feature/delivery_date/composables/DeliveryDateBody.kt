package com.vodovoz.app.feature.delivery_date.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.chip.VodovozChip
import com.vodovoz.app.design_system.composables.tab_row.VodovozScrollableTabRow
import com.vodovoz.app.design_system.composables.tab_row.VodovozTab
import com.vodovoz.app.design_system.composables.tab_row.VodovozTabRow
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateOptionUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.vodovoz.app.util.extensions.indexOfOrNull

@Suppress("NonSkippableComposable")
@Composable
fun DeliveryDateBody(
    modifier: Modifier = Modifier,
    options: List<DeliveryDateOptionUi>,
    selectedOption: DeliveryDateOptionUi,
    timeSections: List<SectionUi<DeliveryTimeIntervalUi>>,
    selectedTimeSection: SectionUi<DeliveryTimeIntervalUi>,
    onTimeSectionSelect: (SectionUi<DeliveryTimeIntervalUi>) -> Unit,
    onOptionSelect: (DeliveryDateOptionUi) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        VodovozScrollableTabRow(
            modifier = Modifier.padding(vertical = 16.dp),
            selectedTabIndex = options.indexOfOrNull(selectedOption) ?: 0,
            edgePadding = 16.dp,
            spacing = 12.dp
        ) {
            options.forEach { option ->
                VodovozChip(
                    text = option.name,
                    selected = selectedOption == option,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                    onSelect = { onOptionSelect(option) },
                    shape = RoundedCornerShape(20.dp),
                    borderStroke = null,
                )
            }
        }

        VodovozTabRow(
            modifier = Modifier.padding(16.dp),
            selectedTabPosition = timeSections.indexOfOrNull(selectedTimeSection) ?: 0
        ) {
            timeSections.forEachIndexed { index, timeSection ->
                key(timeSection.title) {
                    VodovozTab(
                        title = timeSection.title,
                        position = index,
                        selected = timeSection == selectedTimeSection
                    ) { onTimeSectionSelect(timeSection) }
                }
            }
        }


    }
}