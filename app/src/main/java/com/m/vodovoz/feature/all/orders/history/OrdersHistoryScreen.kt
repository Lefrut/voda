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
                        currentFilters = viewState.currentFilters,
                        items = viewState.items,
                        itemsLoading = viewState.loadStates.refresh is LoadState.Loading,
                        appendItems = viewState.loadStates.append is LoadState.Loading,
                        filters = viewState.filters,
                        banners = viewState.banners,
                        onProductSee = { i ->
                            viewModel.notifyPaging(i)
                        },
                        onFilterSelect = { filter ->
                            viewModel.selectFilter(filter)
                        },
                        onAllFiltersSelect = {
                            viewModel.selectAllFilters()
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