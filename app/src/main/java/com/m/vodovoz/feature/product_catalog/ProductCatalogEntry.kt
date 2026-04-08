package com.m.vodovoz.feature.product_catalog

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToCatalog
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToProductFilters
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.feature.product_catalog.api.ProductCatalogNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents
import com.m.vodovoz.util.extensions.shareText

@Composable
fun ProductCatalogEntry(navKey: ProductCatalogNavKey) =
    NavigationEntry<ProductCatalogViewModel, ProductCatalogViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val context = LocalContext.current

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


    viewModel.collectEvents { event ->
        when (event) {
            ProductCatalogViewModel.ProductCatalogEvent.GoBack -> {
                navigator.goBack()
            }

            is ProductCatalogViewModel.ProductCatalogEvent.GoToSearch -> {
                navigator.navigateToSearch(event.query)
            }

            is ProductCatalogViewModel.ProductCatalogEvent.GoToCategories -> {
                navigator.navigateToCategories(
                    category = event.currentCategory,
                    categories = event.categories
                )
            }

            is ProductCatalogViewModel.ProductCatalogEvent.GoToProductDetails -> {
                navigator.navigateToProductDetails(event.productId)
            }

            ProductCatalogViewModel.ProductCatalogEvent.ScrollToTop -> {
                lazyGridState.animateScrollToItem(0)
            }

            is ProductCatalogViewModel.ProductCatalogEvent.GoToProductFilters -> {
                navigator.navigateToProductFilters(
                    event.categoryId,
                    event.filters
                )
            }

            is ProductCatalogViewModel.ProductCatalogEvent.Share -> {
                context.shareText(event.text)
            }

            is ProductCatalogViewModel.ProductCatalogEvent.GoToProductAnalogs -> {
                navigator.navigateToProductAnalogs(event.productId)
            }

            ProductCatalogViewModel.ProductCatalogEvent.GoToQrCode -> {
                navigator.navigateToQrCode()
            }

            ProductCatalogViewModel.ProductCatalogEvent.GoToSpeech -> {
                navigator.navigateToSpeechDialog()
            }

            ProductCatalogViewModel.ProductCatalogEvent.GoToCatalog -> {
                navigator.navigateToCatalog()
            }
        }
    }
}
