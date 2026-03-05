package com.m.vodovoz.feature.cart.bottles

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun AllBottlesEntry() = NavigationEntry<AllBottlesFlowViewModel> {
    val viewState by viewModel.collectAsState()

    Crossfade(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        targetState = viewState.uiState,
        label = "all bottles screen crossfade"
    ) { state ->
        when (state) {
            AllBottlesFlowViewModel.BottlesUiState.Error -> NetworkErrorPlaceholder {
                viewModel.fetchAllBottlesDetails()
            }

            AllBottlesFlowViewModel.BottlesUiState.Loading -> LoadingPlaceholder()
            AllBottlesFlowViewModel.BottlesUiState.Success -> AllBottlesScreen(
                viewModel = viewModel,
                viewState = viewState
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                AllBottlesFlowViewModel.BottlesEvent.GoBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
}
