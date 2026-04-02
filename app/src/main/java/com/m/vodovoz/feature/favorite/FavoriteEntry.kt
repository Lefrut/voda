package com.m.vodovoz.feature.favorite

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.navigateToCatalog
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProfile
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.main.Navigator
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FavoriteEntry(
    viewModel: FavoriteFlowViewModel,
) {
    val viewState by viewModel.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val navigator = LocalNavigator.current

    LifecycleEffect(Unit) {
        viewModel.fetchFavoritesIfChanges()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.fetchFavoritesIfChanges()
        }
    }

    when (viewState.uiState) {
        FavoriteFlowViewModel.FavoriteUiState.Error -> {
            NetworkErrorPlaceholder(
                onTryAgainClick = {
                    viewModel.fetchFavoriteProducts()
                }
            )
        }

        else -> {
            FavoriteScreen(
                viewModel = viewModel,
                viewState = viewState,
                lazyGridState = lazyGridState
            )
        }
    }

    LifecycleEffect {
        observeEvents(viewModel, navigator, lazyGridState)
    }
}

private suspend fun observeEvents(
    viewModel: FavoriteFlowViewModel,
    navigator: Navigator,
    lazyGridState: LazyGridState,
) {
    viewModel.events.collect { event ->
        when (event) {
            is FavoriteFlowViewModel.FavoriteEvents.GoToProfile -> {
                navigator.navigateToProfile(viewModel.tabManager)
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToCategories -> {
                navigator.navigateToCategories(
                    categories = event.categories,
                    category = event.category,
                )
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToProductDetails -> {
                navigator.navigateToProductDetails(event.productId)
            }

            FavoriteFlowViewModel.FavoriteEvents.ScrollToTop -> {
                lazyGridState.animateScrollToItem(0)
            }

            FavoriteFlowViewModel.FavoriteEvents.GoToSearch -> {
                navigator.navigateToSearch()
            }

            FavoriteFlowViewModel.FavoriteEvents.GoToCatalog -> {
                navigator.navigateToCatalog()
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToProductAnalogs -> {
                navigator.navigateToProductAnalogs(event.productId)
            }
        }
    }
}
