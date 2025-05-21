package com.vodovoz.app.feature.productlistnofilter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.rememberAsyncImagePainter
import com.vodovoz.app.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.vodovoz.app.design_system.composables.placeholders.EmptyResultPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozSearchTopBar
import com.vodovoz.app.feature.productlistnofilter.composables.CategoriesBottomSheet
import com.vodovoz.app.feature.productlistnofilter.composables.ProductCatalogBody


@OptIn(ExperimentalMaterial3Api::class)
@Suppress("NonSkippableComposable")
@Composable
fun ProductCatalogScreen(
    viewModel: ProductCatalogViewModel,
    viewState: ProductCatalogViewModel.ProductListNoFilterState,
    lazyGridState: LazyGridState,
) {
    val productsSection = viewState.productsSection
    val pullRefreshState = rememberPullToRefreshState()



    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val searchQuery =
            (viewModel.dataSource as? ProductCatalogFragment.DataSource.Search)?.query
                ?: ""

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


        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            state = pullRefreshState,
            isRefreshing = viewState.showRefreshIndicator,
            onRefresh = { viewModel.refresh() },
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    state = pullRefreshState,
                    containerColor = MaterialTheme.colorScheme.background,
                    color = MaterialTheme.colorScheme.primary,
                    isRefreshing = viewState.showRefreshIndicator
                )
            }
        ) {

            when (val uiState = viewState.uiState) {
                ProductCatalogViewModel.UiState.Error -> {
                    NetworkErrorPlaceholder { viewModel.refresh() }
                }

                ProductCatalogViewModel.UiState.Loading -> {
                    LoadingPlaceholder()
                }

                ProductCatalogViewModel.UiState.Body -> {
                    ProductCatalogBody(
                        lazyGridState = lazyGridState,
                        title = productsSection.title,
                        productsQuantity = productsSection.productsQuantityText,
                        categories = productsSection.categories,
                        currentCategory = viewState.currentCategory,
                        currentSort = viewState.currentSort,
                        products = viewState.products,
                        productsLoadStates = viewState.productsLoadStates,
                        isGridView = viewState.isGridView,
                        showFilters = viewState.showFilters,
                        showEmptyCategory = viewState.showEmptyCategory,
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
                        }
                    )

                }

                is ProductCatalogViewModel.UiState.Empty -> {
                    val placeholder = uiState.placeholder
                    EmptyResultPlaceholder(
                        title = placeholder.headerHtml,
                        description = placeholder.descriptionHtml,
                        imagePainter = rememberAsyncImagePainter(placeholder.imageUrl)
                    )
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
