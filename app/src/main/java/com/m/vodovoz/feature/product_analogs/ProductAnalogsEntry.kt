package com.m.vodovoz.feature.product_analogs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_analogs.api.ProductAnalogsNavKey
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsEvent
import com.m.vodovoz.ui.mvi.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAnalogsEntry(navKey: ProductAnalogsNavKey? = null) =
    NavigationEntry<ProductAnalogsViewModel, ProductAnalogsViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val viewState by viewModel.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchProductAnalogs()
    }

    ProductAnalogsScreen(viewModel = viewModel, viewState = viewState)

    if (viewState.showSortOptionsBottomSheet) {
        SortOptionsBottomSheet(
            onDismissRequest = { viewModel.closeSortOptionsBottomSheet() },
            currentSort = viewState.currentSort,
            sorting = viewState.productsSection.sorting,
            onSortSelect = { sort ->
                viewModel.selectSort(sort)
            }
        )
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                ProductAnalogsEvent.GoBack -> {
                    navigator.goBack()
                }

                is ProductAnalogsEvent.GoToProductAnalogs -> {
                    navigator.navigateToProductDetails(event.productId)
                }

                is ProductAnalogsEvent.GoToProductDetails -> {
                    navigator.navigateToProductDetails(event.productId)
                }
            }
        }
    }
}
