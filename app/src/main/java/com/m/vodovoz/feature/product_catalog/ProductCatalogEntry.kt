package com.m.vodovoz.feature.product_catalog

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToProductFilters
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.filters.FiltersUi
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.shareText

@Composable
fun ProductCatalogEntry() = NavigationEntry<ProductCatalogViewModel> {
    val viewState by viewModel.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        navController.currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>("category")
            ?.let { category ->
                viewModel.selectCategory(category)
            }

        navController.currentBackStackEntry?.savedStateHandle?.remove<FiltersUi>("filters")
            ?.let { filters ->
                viewModel.changeFilters(filters)
            }

        onStopOrDispose { }
    }

    when (val uiState = viewState.uiState) {
        is ProductCatalogViewModel.ProductCatalogUiState.ForAdults -> {
            ForAdultsPlaceholder(
                forAdults = uiState.forAdultsUi,
                onBackClick = {
                    viewModel.navigateBack()
                },
                onApplyClick = {
                    viewModel.setCanViewAdultProducts()
                }
            )
        }

        else -> {
            ProductCatalogScreen(
                viewModel = viewModel,
                viewState = viewState,
                lazyGridState = lazyGridState
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                ProductCatalogViewModel.ProductCatalogEvent.GoBack -> {
                    navController.popBackStack()
                }

                is ProductCatalogViewModel.ProductCatalogEvent.GoToSearch -> {
                    navController.navigateToSearch(event.query)
                }

                is ProductCatalogViewModel.ProductCatalogEvent.GoToCategories -> {
                    navController.navigateToCategories(
                        category = event.currentCategory,
                        categories = event.categories
                    )
                }

                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductDetails -> {
                    navController.navigateToProductDetails(event.productId)
                }

                ProductCatalogViewModel.ProductCatalogEvent.ScrollToTop -> {
                    lazyGridState.animateScrollToItem(0)
                }

                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductFilters -> {
                    navController.navigateToProductFilters(
                        event.categoryId,
                        event.filters
                    )
                }

                is ProductCatalogViewModel.ProductCatalogEvent.Share -> {
                    context.shareText(event.text)
                }

                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductAnalogs -> {
                    navController.navigateToProductAnalogs(event.productId)
                }

                ProductCatalogViewModel.ProductCatalogEvent.GoToQrCode -> {
                    navController.navigateToQrCode()
                }

                ProductCatalogViewModel.ProductCatalogEvent.GoToSpeech -> {
                    navController.navigateToSpeechDialog()
                }

                ProductCatalogViewModel.ProductCatalogEvent.GoToCatalog -> {
                    navController.popBackStack()
                    viewModel.tabManager.selectTab(R.id.graph_catalog)
                }
            }
        }
    }
}
