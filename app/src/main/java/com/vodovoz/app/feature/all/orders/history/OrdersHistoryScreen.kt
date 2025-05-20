package com.vodovoz.app.feature.all.orders.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.HybridSearchTopBar
import com.vodovoz.app.feature.all.orders.history.composables.OrdersHistoryBody

@Composable
fun OrdersHistoryScreen(
    viewModel: AllOrdersFlowViewModel,
    viewState: AllOrdersFlowViewModel.AllOrdersState,
) {

    val uiState = viewState.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HybridSearchTopBar(
            title = if (uiState is AllOrdersFlowViewModel.AllOrdersUiState.Empty) {
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
            is AllOrdersFlowViewModel.AllOrdersUiState.Empty -> {
                VodovozPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = { viewModel.navigateToCatalog() }
                )
            }

            AllOrdersFlowViewModel.AllOrdersUiState.Error -> {
                NetworkErrorPlaceholder { viewModel.fetchOrdersHistoryDetails() }
            }

            AllOrdersFlowViewModel.AllOrdersUiState.Body -> {
                OrdersHistoryBody(
                    items = viewState.items,
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
                        viewModel.notifyPagingItems(i)
                    },
                    onItemButtonClick = { ordersHistoryItem ->
                        viewModel.activateOrderItemButton(ordersHistoryItem)
                    },
                    onItemClick = { item ->
                        viewModel.navigateToOrderDetails(item)
                    }
                )
            }

            AllOrdersFlowViewModel.AllOrdersUiState.Loading -> {
                LoadingPlaceholder()
            }
        }
    }
}