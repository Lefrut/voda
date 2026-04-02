package com.m.vodovoz.feature.filter_values

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.filter_values.api.FilterValuesNavKey
import com.m.vodovoz.feature.product_filters.ProductFiltersFlowViewModel
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FilterValuesEntry(navKey: FilterValuesNavKey? = null) =
    NavigationEntry<FilterValuesViewModel, FilterValuesViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val productFiltersViewModel = viewModel(modelClass = ProductFiltersFlowViewModel::class)
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.changeTabVisibility(false)
        onStopOrDispose {
            viewModel.tabManager.changeTabVisibility(true)
        }
    }

    when (viewState.uiState) {
        FilterValuesViewModel.ConcreteFilterUiState.Loading -> {
            LoadingPlaceholder()
        }

        FilterValuesViewModel.ConcreteFilterUiState.Success -> {
            FilterValuesScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                FilterValuesViewModel.ConcreteFilterEvent.GoBack -> {
                    navigator.goBack()
                }

                is FilterValuesViewModel.ConcreteFilterEvent.GoToProductFilters -> {
                    productFiltersViewModel.changeFilter(event.filter)
                    navigator.goBack()
                }
            }
        }
    }
}
