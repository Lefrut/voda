package com.vodovoz.app.feature.all.orders.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.HybridSearchTopBar
import com.vodovoz.app.feature.all.orders.history.composables.OrdersHistoryBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersHistoryScreen(
    viewModel: OrdersHistoryViewModel,
    viewState: OrdersHistoryViewModel.AllOrdersState,
) {

    val uiState = viewState.uiState
    val pullRefreshState = rememberPullToRefreshState()

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
                    OrdersHistoryBody(
                        items = viewState.items,
                        itemsLoading = viewState.loadStates.refresh is LoadState.Loading,
                        appendItems = viewState.loadStates.append is LoadState.Loading,
                        searchMode = viewState.searchMode,
                        currentFilters = viewState.currentFilters,
                        filters = viewState.filters,
                        onFilterSelect = { filter ->
                            viewModel.selectFilter(filter)
                        },
                        onAllFiltersSelect = {
                            viewModel.selectAllFilters()
                        },
                        onProductSee = { i ->
                            viewModel.notifyPaging(i)
                        },
                        onItemButtonClick = { ordersHistoryItem ->
                            viewModel.activateOrderItemButton(ordersHistoryItem)
                        },
                        onItemClick = { item ->
                            viewModel.navigateToOrderDetails(item)
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