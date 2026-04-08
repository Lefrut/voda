package com.m.vodovoz.feature.preorder

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.preorder.api.PreOrderNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun PreOrderEntry(navKey: PreOrderNavKey) =
    NavigationEntry<PreOrderFlowViewModel, PreOrderFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    when (viewState.uiState) {
        PreOrderFlowViewModel.UiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchPreOrderData() }
        }

        PreOrderFlowViewModel.UiState.Loading -> {
            LoadingPlaceholder()
        }

        PreOrderFlowViewModel.UiState.Success -> {
            PreOrderScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchPreOrderData()
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                PreOrderFlowViewModel.PreOrderEvent.GoBack -> {
                    navigator.goBack()
                }

                PreOrderFlowViewModel.PreOrderEvent.HideKeyboard -> {
                    keyboardController?.hide()
                }

                is PreOrderFlowViewModel.PreOrderEvent.ShowSnackbar -> {
                    withTimeoutOrNull(if (event.isVeryShort) 150L else 1200L) {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }

                is PreOrderFlowViewModel.PreOrderEvent.GoToWebView -> {
                    navigator.navigateToWebView(event.url, event.title)
                }
            }
        }
    }
}
