package com.m.vodovoz.feature.product_catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.m.vodovoz.design_system.composables.placeholders.EmptyResultPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.top_bar.VodovozSearchTopBar
import com.m.vodovoz.feature.product_catalog.composables.CategoriesBottomSheet
import com.m.vodovoz.feature.product_catalog.composables.ProductCatalogBody


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    viewModel: ProductCatalogViewModel,
    viewState: ProductCatalogViewModel.ProductCatalogState,
    lazyGridState: LazyGridState,
) {
    val productsSection = viewState.productsSection

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val searchQuery =
            (viewModel.dataSource as? ProductCatalogFragment.DataSource.Search)?.query ?: ""

        VodovozSearchTopBar(
            value = searchQuery,
            onFocus = {
                viewModel.navigateToSearch(searchQuery)
            },
            onMicClick = {
                viewModel.navigateToSpeech()
            },
            onScanClick = {
                viewModel.navigateToQrCode()
            },
            onNavigationClick = {
                viewModel.navigateBack()
            }
        )


        VodovozPullToRefreshBox(
            isRefreshing = viewState.showRefreshIndicator,
            onRefresh = { viewModel.refresh() }
        ) {
            when (val uiState = viewState.uiState) {
                ProductCatalogViewModel.ProductCatalogUiState.Error -> {
                    NetworkErrorPlaceholder { viewModel.refresh() }
                }

                ProductCatalogViewModel.ProductCatalogUiState.Loading -> {
                    LoadingPlaceholder()
                }

                ProductCatalogViewModel.ProductCatalogUiState.Body -> {
                    ProductCatalogBody(
                        lazyGridState = lazyGridState,
                        title = productsSection.title,
                        productsQuantity = productsSection.productsQuantityText,
                        categories = productsSection.categories,
                        currentCategory = viewState.currentCategory,
                        currentSort = viewState.currentSort,
                        products = viewState.items,
                        productsLoadStates = viewState.loadStates,
                        isGridView = viewState.isGridView,
                        showFilters = viewState.showFilters,
                        showEmptyCategory = viewState.showEmptyCategory,
                        showShare = viewState.showShare,
                        categoriesTree = viewState.categoryTree,
                        onProductSee = { index ->
                            viewModel.notifyPaging(index)
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
                        onCategoriesListClick = {
                            viewModel.showCategoriesBottomSheet()
                        },
                        onProductClick = { product ->
                            viewModel.navigateToProductDetails(product)
                        },
                        onProductLike = { product ->
                            viewModel.changeFavorite(product)
                        },
                        onFiltersClick = {
                            viewModel.navigateToProductFilters()
                        },
                        onShareClick = {
                            viewModel.shareProducts()
                        },
                        onDecrementProductToCart = { product ->
                            viewModel.decrementProductToCart(product)
                        },
                        onIncrementProductToCart = { product ->
                            viewModel.incrementProductToCart(product)

                        },
                        onProductAnalogsClick = { product ->
                            viewModel.navigateToProductAnalogs(product)
                        },
                    )

                }

                is ProductCatalogViewModel.ProductCatalogUiState.Empty -> {
                    val placeholder = uiState.placeholder
                    EmptyResultPlaceholder(
                        title = placeholder.headerHtml,
                        description = placeholder.descriptionHtml,
                        imagePainter = rememberAsyncImagePainter(placeholder.imageUrl)
                    )
                }

                is ProductCatalogViewModel.ProductCatalogUiState.ForAdults -> {

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

    if (viewState.showCategoriesBottomSheet) {
        CategoriesBottomSheet(
            categories = viewState.categoryTree,
            currentCategory = viewState.currentBottomSheetCategory,
            onDismissRequest = {
                viewModel.hideCategoriesBottomSheet()
            },
            onCategoryClick = { category ->
                viewModel.selectBottomSheetCategory(category)
            },
            onCategoryChoose = {
                viewModel.chooseBottomSheetCategory()
            }
        )
    }

}
