package com.m.vodovoz.feature.all.orders.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.top_bar.HybridSearchTopBar
import com.m.vodovoz.feature.all.orders.history.composables.OrdersHistoryBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    viewModel: OrdersHistoryViewModel,
    viewState: OrdersHistoryViewModel.AllOrdersState,
) {

    val uiState = viewState.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HybridSearchTopBar(
            title = if (uiState is OrdersHistoryViewModel.AllOrdersUiState.Empty) {
                uiState.placeholder.title
            } else {
                viewState.title
            },
            searchQuery = viewState.searchQuery,
            isSearchMode = viewState.searchMode,
            onSearchModeChange = { searchMode ->
                viewModel.changeMode(searchMode)
            },
            onSearchQueryChange = { query ->
                viewModel.changeSearchQuery(query)
            },
            onNavigationClick = {
                viewModel.navigateBack()
            }
        )
        when (uiState) {
            is OrdersHistoryViewModel.AllOrdersUiState.Empty -> {
                VodovozPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = { viewModel.navigateToCatalog() }
                )
            }

            OrdersHistoryViewModel.AllOrdersUiState.Error -> {
                NetworkErrorPlaceholder { viewModel.fetchOrdersHistoryDetails() }
            }

            OrdersHistoryViewModel.AllOrdersUiState.Body -> {
                VodovozPullToRefreshBox(
                    isRefreshing = viewState.showRefreshIndicator,
                    onRefresh = { viewModel.refresh() },
                ) {
                    OrdersHistoryBody(
                        searchMode = viewState.searchMode,
                        tabs = viewState.tabs,
                        selectedTabIndex = viewState.selectedTabIndex,
                        currentYear = viewState.currentYear,
                        currentTabPlaceholder = viewState.currentTabPlaceholder,
                        orders = viewState.items1,
                        itemsLoading = viewState.loadStates1.refresh is LoadState.Loading,
                        appendItems = viewState.loadStates1.append is LoadState.Loading ||
                                viewState.loadStates2.refresh is LoadState.Loading ||
                                viewState.loadStates2.append is LoadState.Loading,
                        products = viewState.items2,
                        productsTitle = viewState.productsTitle,
                        banners = viewState.banners,
                        onOrderSee = { i ->
                            viewModel.notifyPaging1(i)
                        },
                        onProductSee = { i ->
                            viewModel.notifyPaging2(i)
                        },
                        onTabSelect = { index ->
                            viewModel.selectTab(index)
                        },
                        onYearSelect = { year ->
                            viewModel.selectYear(year)
                        },
                        onItemClick = { item ->
                            viewModel.navigateToOrderDetails(item)
                        },
                        onBannerClick = { it ->
                            viewModel.activateBanner(it)
                        },
                        onAboutAdvertisingClick = {
                            viewModel.showAboutAdvertisingBS(it)
                        },
                        onItemButtonClick = { ordersHistoryItem ->
                            viewModel.activateOrderItemButton(ordersHistoryItem)
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
                        },
                        onPlaceholderButtonClick = {
                            viewModel.navigateToCatalog()
                        }
                    )
                }
            }

            OrdersHistoryViewModel.AllOrdersUiState.Loading -> {
                LoadingPlaceholder()
            }
        }
    }
}
