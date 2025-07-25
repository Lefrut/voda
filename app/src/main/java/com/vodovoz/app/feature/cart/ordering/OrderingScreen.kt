package com.vodovoz.app.feature.cart.ordering

import androidx.compose.foundation.ScrollState
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
import androidx.compose.ui.platform.LocalContext
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.cart.ordering.composables.OrderingBody
import com.vodovoz.app.util.extensions.deviceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderingScreen(
    viewModel: OrderingFlowViewModel,
    viewState: OrderingFlowViewModel.OrderingState,
    scrollState: ScrollState,
) {
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            title = viewState.title,
            onBack = {
                viewModel.navigateBack()
            }
        )
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize(),
            state = pullRefreshState,
            isRefreshing = viewState.showRefreshIndicator,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = viewState.showRefreshIndicator,
                    state = pullRefreshState,
                    containerColor = MaterialTheme.colorScheme.background,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            onRefresh = {
                viewModel.refreshRecipient()
            }
        ) {
            when (viewState.uiState) {
                OrderingFlowViewModel.OrderingUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchOrderingDetails()
                    }
                }

                OrderingFlowViewModel.OrderingUiState.Loading -> {
                    LoadingPlaceholder()
                }

                OrderingFlowViewModel.OrderingUiState.Order -> {
                    OrderingBody(
                        scrollState = scrollState,
                        comment = viewState.comment,
                        paymentSection = viewState.paymentSection,
                        notifySection = viewState.notifySection,
                        selectedNotifyItem = viewState.selectedNotifyItem,
                        recipientSection = viewState.recipientSection,
                        totals = viewState.totals,
                        button = viewState.button,
                        onRecipientItemClick = { orderRecipientItem ->
                            viewModel.navigateByRecipientItem(orderRecipientItem)
                        },
                        onNotifyItemSelect = { notifyItem ->
                            viewModel.selectNotifyItem(notifyItem)
                        },
                        onCommentChange = { field, updatedField ->
                            viewModel.changeComment(field, updatedField)
                        },
                        onPaymentButtonClick = { orderPaymentItem ->
                            viewModel.navigateByPaymentItem(orderPaymentItem)
                        },
                        onButtonClick = {
                            viewModel.doOrder(context.deviceInfo())
                        }
                    )

                }

                is OrderingFlowViewModel.OrderingUiState.Success -> {}
            }
        }
    }
}