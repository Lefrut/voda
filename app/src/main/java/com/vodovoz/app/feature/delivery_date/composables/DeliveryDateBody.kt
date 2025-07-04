package com.vodovoz.app.feature.delivery_date.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottomLine
import com.vodovoz.app.design_system.composables.button.VodovozRadioButton
import com.vodovoz.app.design_system.composables.chip.VodovozChip
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
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
    listPaddingValues: PaddingValues,
    listIsLoading: Boolean,
    options: List<DeliveryDateOptionUi>,
    selectedOption: DeliveryDateOptionUi,
    timeSections: List<SectionUi<DeliveryTimeIntervalUi>>,
    selectedTimeSection: SectionUi<DeliveryTimeIntervalUi>,
    selectedTimeInterval: DeliveryTimeIntervalUi,
    onTimeSectionSelect: (SectionUi<DeliveryTimeIntervalUi>) -> Unit,
    onOptionSelect: (DeliveryDateOptionUi) -> Unit,
    onTimeIntervalSelect: (DeliveryTimeIntervalUi) -> Unit,
    onCalendarShow: () -> Unit,
) {

    val selectedTabOption = options.firstOrNull { it.value == selectedOption.value }

    Column(modifier = modifier.fillMaxSize()) {
        VodovozScrollableTabRow(
            modifier = Modifier.padding(vertical = 16.dp),
            selectedTabIndex = options.indexOfOrNull(selectedTabOption ?: selectedOption) ?: 0,
            edgePadding = 16.dp,
            spacing = 12.dp
        ) {
            options.forEach { option ->
                val selected = selectedOption.value == option.value
                VodovozChip(
                    text = option.name,
                    selected = selected,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                    onSelect = { onOptionSelect(option) },
                    shape = RoundedCornerShape(20.dp),
                    borderStroke = null,
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                )
            }

            val selectedChooseDate = selectedTabOption == null

            VodovozChip(
                text = stringResource(id = R.string.choose_date),
                selected = selectedChooseDate,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
                onSelect = { onCalendarShow() },
                shape = RoundedCornerShape(20.dp),
                borderStroke = null,
                containerColor = if (selectedChooseDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (selectedChooseDate) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
            )

        }

        if (timeSections.isNotEmpty()) {
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

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(listPaddingValues)
                .padding(
                    top = 16.dp,
                    bottom = 24.dp
                )
        ) {
            val intervals = selectedTimeSection.items
            when {
                listIsLoading -> {
                    LoadingPlaceholder(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                intervals.isNotEmpty() -> {
                    intervals.forEachIndexed { _, deliveryTimeInterval ->
                        DeliveryTimeIntervalItem(
                            modifier = Modifier.bottomLine(MaterialTheme.colorScheme.surfaceVariant),
                            deliveryTimeInterval = deliveryTimeInterval,
                            selected = deliveryTimeInterval.value == selectedTimeInterval.value,
                            onClick = onTimeIntervalSelect
                        )
                    }
                }

                selectedTimeSection.placeholder != null -> {
                    VodovozPlaceholder(data = selectedTimeSection.placeholder)
                }
            }
        }


    }
}

@Composable
private fun DeliveryTimeIntervalItem(
    modifier: Modifier = Modifier,
    deliveryTimeInterval: DeliveryTimeIntervalUi,
    selected: Boolean,
    onClick: (DeliveryTimeIntervalUi) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(44.dp)
            .clickable { onClick(deliveryTimeInterval) }
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VodovozRadioButton(
            modifier = Modifier.padding(end = 16.dp),
            selected = selected,
            onClick = { onClick(deliveryTimeInterval) },
        )

        Text(
            modifier = Modifier.weight(1f),
            text = deliveryTimeInterval.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )

        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = deliveryTimeInterval.priceText,
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}