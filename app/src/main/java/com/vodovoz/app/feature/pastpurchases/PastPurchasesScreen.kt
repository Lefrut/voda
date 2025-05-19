package com.vodovoz.app.feature.pastpurchases

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.pastpurchases.composables.PastPurchasesBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastPurchasesScreen(
    viewModel: PastPurchasesFlowViewModel,
    viewState: PastPurchasesFlowViewModel.PastPurchasesState,
    lazyGridState: LazyGridState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VodovozTopBar(
            title = viewState.title,
            actionPainter = painterResource(id = R.drawable.icon_search),
            onActionClick = {
                viewModel.navigateToSearch()
            },
            onBack = {
                viewModel.navigateBack()
            }
        )

        when (val uiState = viewState.uiState) {
            is PastPurchasesFlowViewModel.PastPurchasesUiState.Empty -> {
                VodovozPlaceholder(data = uiState.placeholder, onButtonClick = { viewModel.navigateToCatalog() })
            }

            PastPurchasesFlowViewModel.PastPurchasesUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchPastPurchasesDetails()
                }
            }

            PastPurchasesFlowViewModel.PastPurchasesUiState.Loading -> {
                LoadingPlaceholder()
            }

            PastPurchasesFlowViewModel.PastPurchasesUiState.Success -> {
                PastPurchasesBody(
                    modifier = Modifier.weight(1f),
                    lazyGridState = lazyGridState,
                    categories = viewState.categories,
                    currentCategory = viewState.currentCategory,
                    currentSort = viewState.currentSort,
                    isGridView = viewState.isGridView,
                    products = viewState.products,
                    productsLoadStates = viewState.productsLoadStates,
                    onProductSee = { index ->
                        viewModel.notifyPagingProducts(index)
                    },
                    onSortingClick = {
                        viewModel.showSortBottomSheet()
                    },
                    onSwitchLayoutClick = {
                        viewModel.switchLayout()
                    },
                    onCategoryClick = { category ->
                        viewModel.selectCategory(category)
                    },
                    onProductClick = { product ->
                        viewModel.navigateToProductDetails(product)
                    },
                    onProductLike = { product ->
                        viewModel.changeFavorite(product)
                    },
                    onProductAnalogsClick = { product ->
                        viewModel.navigateToProductAnalogs(product)
                    },
                    onIncrementProductToCart = { product ->
                        viewModel.incrementProductToCart(product)
                    },
                    onDecrementProductToCart = { product ->
                        viewModel.decrementProductToCart(product)
                    }
                )

            }
        }


    }

    if (viewState.showSortBottomSheet) {
        SortOptionsBottomSheet(
            onDismissRequest = { viewModel.hideSortBottomSheet() },
            currentSort = viewState.currentSort,
            sorting = viewState.sorting,
            onSortSelect = { sort -> viewModel.selectSort(sort) }
        )
    }

}