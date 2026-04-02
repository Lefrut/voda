package com.m.vodovoz.feature.cancel_order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.feature.cancel_order.api.CancelOrderNavKey
import com.m.vodovoz.feature.cancel_order.model.CancelOrderEvent
import com.m.vodovoz.feature.cancel_order.model.CancelOrderUiState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents

@Composable
fun CancelOrderEntry(navKey: CancelOrderNavKey? = null) =
    NavigationEntry<CancelOrderViewModel, CancelOrderViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    DisposableEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onDispose {
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    when (viewState.uiState) {
        CancelOrderUiState.Body -> {
            CancelOrderScreen(
                viewModel = viewModel,
                viewState = viewState
            )
        }

        CancelOrderUiState.Error -> {
            NetworkErrorPlaceholder {
                viewModel.fetchCancelOrderDetails()
            }
        }

        CancelOrderUiState.Loading -> {
            LoadingPlaceholder()
        }
    }

    viewModel.collectEvents { event ->
        when (event) {
            CancelOrderEvent.GoBack -> {
                navigator.goBack()
            }
        }
    }
}
