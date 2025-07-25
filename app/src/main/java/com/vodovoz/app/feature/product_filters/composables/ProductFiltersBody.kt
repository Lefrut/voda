package com.vodovoz.app.feature.product_filters.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.chip.VodovozChip
import com.vodovoz.app.design_system.model.filters.FilterUi
import com.vodovoz.app.design_system.model.filters.FilterValueUi
import com.vodovoz.app.design_system.model.filters.FiltersPriceUi
import com.vodovoz.app.util.extensions.calculateActiveRange
import com.vodovoz.app.util.extensions.debugLog
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NonSkippableComposable")
@Composable
fun ProductFiltersBody(
    modifier: Modifier = Modifier,
    sliderState: RangeSliderState,
    filterPrice: FiltersPriceUi,
    filters: List<FilterUi>,
    onPriceRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onFilterRangeChange: (FilterUi, ClosedFloatingPointRange<Float>) -> Unit,
    onFilterValueSelect: (FilterUi, FilterValueUi) -> Unit,
    onFilterFromChange: (FilterUi, Float) -> Unit,
    onFilterToChange: (FilterUi, Float) -> Unit,
    onShowAllFilterValuesClick: (FilterUi) -> Unit,
    onPriceFromChange: (String) -> Unit,
    onPriceToChange: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            text = stringResource(R.string.price_and_currency),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )

        ProductFilterSlider(
            sliderState = sliderState,
            currentMaxText = filterPrice.currentMax.toString(),
            currentMinText = filterPrice.currentMin.toString(),
            onFromChange = { onPriceFromChange(it.toString()) },
            onToChange = { onPriceToChange(it.toString()) },
            onSliderRangeChange = onPriceRangeChange
        )

        filters.forEachIndexed { _, filter ->
            key(filter.name + filter.id) {
                FilterItem(
                    filter = filter,
                    onFilterValueSelect = { _, value ->
                        onFilterValueSelect(filter, value)
                    },
                    onShowAllClick = {
                        onShowAllFilterValuesClick(filter)
                    },
                    onFilterSliderChange = onFilterRangeChange,
                    onFilterFromChange = onFilterFromChange,
                    onFilterToChange = onFilterToChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun FilterItem(
    modifier: Modifier = Modifier,
    filter: FilterUi,
    onFilterValueSelect: (FilterUi, FilterValueUi) -> Unit,
    onFilterSliderChange: (FilterUi, ClosedFloatingPointRange<Float>) -> Unit,
    onFilterFromChange: (FilterUi, Float) -> Unit,
    onFilterToChange: (FilterUi, Float) -> Unit,
    onShowAllClick: (FilterUi) -> Unit,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(top = 10.dp, start = 16.dp, end = 16.dp)) {
            Text(
                modifier = Modifier.weight(1f),
                text = filter.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(10.dp))

            if (filter.totalValues > 6 && filter.bounds == null) {
                Text(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onShowAllClick(filter) }
                        .padding(horizontal = 8.dp),
                    text = stringResource(id = R.string.all),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        val sliderBounds = filter.bounds
        when {
            sliderBounds != null -> {
                val currentSliderBounds = filter.currentBounds ?: sliderBounds

                val sliderState = remember(
                    sliderBounds.start,
                    sliderBounds.endInclusive
                ) {
                    val range = calculateActiveRange(
                        max = sliderBounds.endInclusive,
                        min = sliderBounds.start,
                        currentMax = currentSliderBounds.endInclusive,
                        currentMin = currentSliderBounds.start
                    )
                    RangeSliderState(
                        activeRangeStart = range.start,
                        activeRangeEnd = range.endInclusive,
                        valueRange = 0f..1f
                    )
                }

                ProductFilterSlider(
                    sliderState = sliderState,
                    currentMinText = currentSliderBounds.start.toString(),
                    currentMaxText = currentSliderBounds.endInclusive.toString(),
                    onFromChange = { from ->
                        onFilterFromChange(filter, from)
                    },
                    onToChange = { to ->
                        onFilterToChange(filter, to)
                    },
                    onSliderRangeChange = { sliderRange ->
                        onFilterSliderChange(filter, sliderRange)
                    }
                )

                val activeRangeStart = rememberUpdatedState(newValue = currentSliderBounds.start)
                val activeRangeEnd = rememberUpdatedState(newValue = currentSliderBounds.endInclusive)

                LaunchedEffect(Unit) {
                    snapshotFlow { activeRangeStart.value }
                        .debounce(1200)
                        .collectLatest { first ->

                            val start = calculateActiveRange(
                                max = sliderBounds.endInclusive,
                                min = sliderBounds.start,
                                currentMax = currentSliderBounds.endInclusive,
                                currentMin = first
                            ).start

                            debugLog { "calculate start for $first - $start" }

                            sliderState.activeRangeStart = start
                        }
                }

                LaunchedEffect(Unit) {
                    snapshotFlow { activeRangeEnd.value }
                        .debounce(1200)
                        .collectLatest { last ->
                            val end = calculateActiveRange(
                                max = sliderBounds.endInclusive,
                                min = sliderBounds.start,
                                currentMax = last,
                                currentMin = currentSliderBounds.start
                            ).endInclusive

                            sliderState.activeRangeEnd = end
                        }
                }
            }

            else -> {
                FilterChips(
                    filter = filter,
                    onFilterValueSelect = onFilterValueSelect
                )
            }
        }
    }
}

@Composable
private fun FilterChips(
    modifier: Modifier = Modifier,
    filter: FilterUi,
    onFilterValueSelect: (FilterUi, FilterValueUi) -> Unit,
) {
    FlowRow(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filterValues = filter.values
        filterValues.takeWhile { filterValue ->
            filterValue.selected || filterValues.indexOf(filterValue) < 6
        }.forEach { filterValue ->
            VodovozChip(
                text = filterValue.name,
                selected = filterValue.selected,
                onSelect = { onFilterValueSelect(filter, filterValue) },
                containerColor = if (filterValue.selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (filterValue.selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
            )
        }
    }

}