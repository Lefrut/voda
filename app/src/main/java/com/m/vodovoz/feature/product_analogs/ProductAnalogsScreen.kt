package com.m.vodovoz.feature.product_analogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.list.ProductLazyList
import com.m.vodovoz.design_system.composables.list.ProductListOptionsRow
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.product_analogs.composables.ProductsCollectionPlaceholder
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsState
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsUiState

@Composable
fun ProductAnalogsScreen(
    viewModel: ProductAnalogsViewModel,
    viewState: ProductAnalogsState,
) {
    val productsSection = viewState.productsSection
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .consumeWindowInsets(WindowInsets.systemBars)
    ) {
        VodovozTopBar(
            title = productsSection.title,
            onBack = {
                viewModel.navigateBack()
            },
        )

        when(viewState.uiState){
            ProductAnalogsUiState.Loading -> {
                ProductsCollectionPlaceholder()
            }
            ProductAnalogsUiState.Success -> {
                ProductListOptionsRow(
                    modifier = Modifier.padding(top = 8.dp),
                    sortName = viewState.currentSort.name,
                    isGridView = viewState.isGridView,
                    onSwitchClick = {
                        viewModel.switchLayout()
                    },
                    onSortingClick = {
                        viewModel.showSortOptionsBottomSheet()
                    }
                )


                val products = productsSection.products

                ProductLazyList(
                    products = products,
                    isGridView = viewState.isGridView,
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductLike = { product ->
                        viewModel.changeProductFavorite(product)
                    },
                    onProductAnalogsClick = { product ->
                        viewModel.navigateToProductAnalogs(product)
                    },
                    onIncrementProductToCart = { product ->
                        viewModel.incrementProductToCart(product)
                    },
                    onDecrementProductToCart = { product ->
                        viewModel.decrementProductToCart(product)
                    },
                )
            }

            ProductAnalogsUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchProductAnalogs()
                }
            }
        }

    }
}