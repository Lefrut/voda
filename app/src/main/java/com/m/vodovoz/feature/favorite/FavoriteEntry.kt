package com.m.vodovoz.feature.favorite

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.navigation.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun FavoriteEntry(
    viewModel: FavoriteFlowViewModel,
) {
    val viewState by viewModel.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val navController = LocalView.current.findNavController()

    LifecycleEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>("category")
            ?.let { category -> viewModel.selectCategory(category) }
            ?: viewModel.fetchFavoritesIfChanges()
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
        observeEvents(viewModel, navController, lazyGridState)
    }
}

private suspend fun observeEvents(
    viewModel: FavoriteFlowViewModel,
    navController: androidx.navigation.NavController,
    lazyGridState: LazyGridState,
) {
    viewModel.events.collect { event ->
        when (event) {
            is FavoriteFlowViewModel.FavoriteEvents.GoToProfile -> {
                viewModel.tabManager.setAuthRedirect(navController.graph.id)
                viewModel.tabManager.selectTab(R.id.graph_profile)
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToCategories -> {
                navController.navigateToCategories(
                    categories = event.categories,
                    category = event.category,
                )
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToProductDetails -> {
                navController.navigateToProductDetails(event.productId)
            }

            FavoriteFlowViewModel.FavoriteEvents.ScrollToTop -> {
                lazyGridState.animateScrollToItem(0)
            }

            FavoriteFlowViewModel.FavoriteEvents.GoToSearch -> {
                navController.navigateToSearch()
            }

            FavoriteFlowViewModel.FavoriteEvents.GoToCatalog -> {
                viewModel.tabManager.selectTab(R.id.graph_catalog)
                navController.popBackStack(R.id.catalogFragment, false)
            }

            is FavoriteFlowViewModel.FavoriteEvents.GoToProductAnalogs -> {
                navController.navigateToProductAnalogs(event.productId)
            }
        }
    }
}
