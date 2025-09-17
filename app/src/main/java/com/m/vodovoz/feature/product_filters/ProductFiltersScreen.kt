package com.m.vodovoz.feature.product_filters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.feature.product_filters.composables.ProductFiltersBody
import com.m.vodovoz.feature.product_filters.composables.ProductFiltersTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFiltersScreen(
    viewModel: ProductFiltersFlowViewModel,
    viewState: ProductFiltersFlowViewModel.ProductFiltersState,
    sliderState: RangeSliderState
) {
    val filters = viewState.filters
    Scaffold(
        topBar = {
            ProductFiltersTopBar(showClearButton = viewState.showClearButton, onClearClick = { viewModel.clearFilters() }) { viewModel.navigateBack() }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = viewState.showApplyButton,
                enter = slideInVertically { it },
                exit = slideOutVertically { -it }
            ) {
                BottomFloatingContainer {
                    VodovozButton(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.apply),
                        onClick = {
                            viewModel.navigateToProductList()
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        ProductFiltersBody(
            modifier = Modifier.padding(paddingValues),
            sliderState = sliderState,
            filters = filters.filters,
            filterPrice = filters.price,
            onPriceRangeChange = { range ->
                viewModel.changeFiltersPrice(range)
            },
            onFilterValueSelect = { filter, filterValue ->
                viewModel.selectFilterValue(filter, filterValue)
            },
            onShowAllFilterValuesClick = { filterUi ->
                viewModel.navigateToFilterValues(filterUi)
            },
            onPriceFromChange = {
                viewModel.changePriceFromField(it)
            },
            onPriceToChange = {
                viewModel.changePriceToField(it)
            },
            onFilterRangeChange = { filter, range ->
                viewModel.changeFilterRange(filter, range)
            },
            onFilterFromChange = { filter, value ->
                viewModel.changeFilterFrom(filter, value)
            },
            onFilterToChange = { filter, value ->
                viewModel.changeFilterTo(filter, value)
            }
        )
    }
}