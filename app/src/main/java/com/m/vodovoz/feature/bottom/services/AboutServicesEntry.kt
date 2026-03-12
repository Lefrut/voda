package com.m.vodovoz.feature.bottom.services

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToServiceDetails
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun AboutServicesEntry() = NavigationEntry<AboutServicesFlowViewModel> {
    val viewState by viewModel.collectAsState()

    when (viewState.uiState) {
        AboutServicesFlowViewModel.AboutServicesUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchAboutServicesDetails() }
        }

        AboutServicesFlowViewModel.AboutServicesUiState.Loading -> {
            LoadingPlaceholder()
        }

        AboutServicesFlowViewModel.AboutServicesUiState.Success -> {
            AboutServicesScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                AboutServicesFlowViewModel.AboutServicesEvents.GoBack -> {
                    navigator.goBack()
                }

                is AboutServicesFlowViewModel.AboutServicesEvents.GoToServiceDetails -> {
                    navigator.navigateToServiceDetails(event.serviceId)
                }
            }
        }
    }
}
