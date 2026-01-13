package com.m.vodovoz.feature.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.m.vodovoz.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.feature.favorite.composables.FavoriteBody
import com.m.vodovoz.feature.favorite.composables.FavoriteTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteFlowViewModel,
    viewState: FavoriteFlowViewModel.FavoriteState,
    lazyGridState: LazyGridState,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        FavoriteTopBar(
            title = viewState.productsSection.title,
            showSearch = viewState.uiState !is FavoriteFlowViewModel.FavoriteUiState.Error,
            onSearchClick = {
                viewModel.navigateToSearch()
            }
        )

        val categories = viewState.productsSection.categories

        VodovozPullToRefreshBox(
            isRefreshing = viewState.showRefreshIndicator,
            onRefresh = {
                viewModel.refresh()
            }
        ) {

            when (val uiState = viewState.uiState) {
                is FavoriteFlowViewModel.FavoriteUiState.Empty -> {
                    VodovozPlaceholder(
                        data = uiState.placeholder,
                        onButtonClick = { viewModel.navigateToCatalog() },
                        onAnalogsClick = viewModel::navigateToProductAnalogs,
                        onProductLike = viewModel::changeFavorite,
                        onProductClick = viewModel::navigateToProductDetails,
                        onIncrementToCart = viewModel::incrementProductToCart,
                        onDecrementToCart = viewModel::decrementProductToCart
                    )
                }

                FavoriteFlowViewModel.FavoriteUiState.Loading -> {
                    LoadingPlaceholder()
                }

                FavoriteFlowViewModel.FavoriteUiState.Success -> {
                    FavoriteBody(
                        categories = categories,
                        currentCategory = viewState.currentCategory,
                        currentSort = viewState.currentSort,
                        isGridView = viewState.isGridView,
                        products = viewState.items,
                        lazyGridState = lazyGridState,
                        productsLoadStates = viewState.loadStates,
                        onCategoriesListClick = {
                            viewModel.navigateToCategories()
                        },
                        onSwitchLayoutClick = {
                            viewModel.switchLayout()
                        },
                        onSortingClick = {
                            viewModel.showSortBottomSheet()
                        },
                        onCategoryClick = { categoryUi ->
                            viewModel.selectCategory(categoryUi)
                        },
                        onProductLike = { product ->
                            viewModel.changeFavorite(product)
                        },
                        onProductClick = { product ->
                            viewModel.navigateToProductDetails(product)
                        },
                        onProductSee = { index ->
                            viewModel.notifyPaging(index)
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

                FavoriteFlowViewModel.FavoriteUiState.Error -> {

                }
            }
        }
    }

    if (viewState.showSortBottomSheet) {
        SortOptionsBottomSheet(
            onDismissRequest = { viewModel.hideSortBottomSheet() },
            currentSort = viewState.currentSort,
            sorting = viewState.productsSection.sorting,
            onSortSelect = { sort -> viewModel.selectSort(sort) }
        )
    }
}