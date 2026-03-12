package com.m.vodovoz.feature.filter_values

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FilterValuesEntry() = NavigationEntry<FilterValuesViewModel> {
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
                    navigator.previousBackStackEntry?.savedStateHandle?.set(
                        "filter",
                        event.filter
                    )
                    navigator.popBackStack(
                        R.id.productFiltersFragment,
                        false
                    )
                }
            }
        }
    }
}
