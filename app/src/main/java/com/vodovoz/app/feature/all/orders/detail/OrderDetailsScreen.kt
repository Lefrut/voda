package com.vodovoz.app.feature.all.orders.detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.vodovoz.app.feature.all.orders.detail.composables.OrderDetailsBody
import com.vodovoz.app.feature.all.orders.detail.composables.OrderDetailsTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    viewModel: OrderDetailsFlowViewModel,
    viewState: OrderDetailsFlowViewModel.OrderDetailsState,
) {
    Scaffold(
        topBar = {
            OrderDetailsTopBar(
                title = viewState.title,
                subtitle = viewState.subtitle,
                onBack = {
                    viewModel.navigateBack()
                },
                onCopy = {
                    viewModel.copyOrderId()
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        VodovozPullToRefreshBox(
            modifier = Modifier.padding(padding),
            isRefreshing = viewState.showRefreshIndicator,
            onRefresh = {
                viewModel.refresh()
            }
        ) {
            OrderDetailsBody(
                header = viewState.header,
                statuses = viewState.statuses,
                currentStatuses = viewState.currentStatuses,
                topButtons = viewState.topButtons,
                productsTitle = viewState.productsTitle,
                products = viewState.items,
                bottomButtons = viewState.bottomButtons,
                questionButton = viewState.questionButton,
                orderSummary = viewState.orderSummary,
                onTopButtonClick = { orderDetailsButton ->
                    viewModel.activateTopButton(orderDetailsButton)
                },
                onBottomButtonClick = { button ->
                    viewModel.activateBottomButton(button)
                },
                onProductLike = { orderProduct ->
                    viewModel.changeProductFavorite(orderProduct)
                },
                onProductClick = { orderProduct ->
                    viewModel.navigateToProductDetails(orderProduct)
                }
            )
        }
    }
}