package com.m.vodovoz.feature.all.brands

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToBrandProductList
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun AllBrandsEntry() = NavigationEntry<AllBrandsFlowViewModel> {
    val viewState by viewModel.collectAsState()

    when (viewState.uiState) {
        AllBrandsFlowViewModel.AllBrandsUiState.Loading -> {
            LoadingPlaceholder()
        }

        AllBrandsFlowViewModel.AllBrandsUiState.Success -> {
            AllBrandsScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                AllBrandsFlowViewModel.AllBrandsEvents.GoBack -> {
                    navigator.goBack()
                }

                is AllBrandsFlowViewModel.AllBrandsEvents.GoToBrandProducts -> {
                    navigator.navigateToBrandProductList(event.brandId)
                }
            }
        }
    }
}
