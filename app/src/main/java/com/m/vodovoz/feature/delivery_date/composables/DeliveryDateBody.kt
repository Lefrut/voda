package com.m.vodovoz.feature.delivery_date.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozRadioButton
import com.m.vodovoz.design_system.composables.chip.VodovozChip
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.tab_row.VodovozScrollableTabRow
import com.m.vodovoz.design_system.composables.tab_row.VodovozTab
import com.m.vodovoz.design_system.composables.tab_row.VodovozTabRow
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.widgets.LabelUi
import com.m.vodovoz.design_system.modifiers.bottomLine
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateOptionUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.m.vodovoz.util.extensions.indexOfOrNull
import com.m.vodovoz.util.extensions.indexOfOrZero

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
            selectedTabIndex = options.indexOfOrNull(selectedTabOption) ?: (options.lastIndex + 1),
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
                selectedTabPosition = timeSections.indexOfFirst {
                    it.title == selectedTimeSection.title
                }
            ) {
                timeSections.forEachIndexed { index, timeSection ->
                    key(timeSection.title) {
                        VodovozTab(
                            title = timeSection.title,
                            position = index,
                            selected = timeSection == selectedTimeSection,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
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
            var minIntervalTextWidth by remember {
                mutableStateOf(0.dp)
            }

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
                            onClick = onTimeIntervalSelect,
                            minIntervalTextWidth = minIntervalTextWidth,
                            onMeasuredWidth = { width -> minIntervalTextWidth = width },
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
    minIntervalTextWidth: Dp,
    onMeasuredWidth: (Dp) -> Unit,
    onClick: (DeliveryTimeIntervalUi) -> Unit,
) {
    val density = LocalDensity.current

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

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f, false),
            ) {
                Text(
                    modifier = Modifier
                        .animateContentSize()
                        .widthIn(min = minIntervalTextWidth)
                        .onSizeChanged { size ->
                            with(density) { onMeasuredWidth(size.width.toDp()) }
                        },
                    text = deliveryTimeInterval.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            deliveryTimeInterval.label?.let { label ->
                DeliveryTimeLabel(modifier = Modifier.padding(start = 8.dp), label = label)
            }
        }


        Text(
            modifier = Modifier.padding(start = 4.dp),
            text = deliveryTimeInterval.priceText,
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.bodyMedium
        )


    }
}

@Composable
private fun DeliveryTimeLabel(modifier: Modifier = Modifier, label: LabelUi) {
    Row(
        modifier = modifier
            .background(
                color = label.backgroundColor,
                shape = MaterialTheme.shapes.medium
            )
            .padding(end = 8.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.name,
            color = label.textColor,
            style = MaterialTheme.typography.labelMedium
        )
    }
}