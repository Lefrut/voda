package com.m.vodovoz.feature.product_filters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductFilterValues
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.filters.FiltersPriceUi
import com.m.vodovoz.feature.product_catalog.ProductCatalogViewModel
import com.m.vodovoz.feature.product_filters.api.ProductFiltersNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.calculateActiveRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberFiltersRangeSliderState(
    uiState: ProductFiltersFlowViewModel.ProductFiltersUiState,
    filtersPrice: FiltersPriceUi,
): RangeSliderState {
    return remember(
        uiState,
        filtersPrice.min,
        filtersPrice.max
    ) {
        val range = calculateActiveRange(
            min = filtersPrice.min,
            max = filtersPrice.max,
            currentMin = filtersPrice.currentMin,
            currentMax = filtersPrice.currentMax
        )

        RangeSliderState(
            activeRangeStart = range.start,
            activeRangeEnd = range.endInclusive,
            valueRange = 0f..1f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFiltersEntry(navKey: ProductFiltersNavKey? = null) =
    NavigationEntry<ProductFiltersFlowViewModel, ProductFiltersFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val productCatalogViewModel = viewModel(modelClass = ProductCatalogViewModel::class)
    val viewState by viewModel.collectAsState()
    val filterPrice = viewState.filters.price

    val sliderState = rememberFiltersRangeSliderState(
        viewState.uiState,
        filterPrice,
    )

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (viewState.uiState) {
        ProductFiltersFlowViewModel.ProductFiltersUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchFiltersByCategory() }
        }

        ProductFiltersFlowViewModel.ProductFiltersUiState.Loading -> {
            LoadingPlaceholder()
        }

        ProductFiltersFlowViewModel.ProductFiltersUiState.Success -> {
            ProductFiltersScreen(
                viewModel = viewModel,
                viewState = viewState,
                sliderState = sliderState
            )
        }
    }

    LifecycleEffect(sliderState) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoBack -> {
                    navigator.goBack()
                }

                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoToFilterValues -> {
                    navigator.navigateToProductFilterValues(
                        event.categoryId,
                        event.filter
                    )
                }

                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoToProductList -> {
                    productCatalogViewModel.changeFilters(event.filters)
                    navigator.goBack()
                }

                ProductFiltersFlowViewModel.ProductFiltersEvent.ResetSlider -> {
                    sliderState.activeRangeStart = 0f
                    sliderState.activeRangeEnd = 1f
                }
            }
        }
    }
}
