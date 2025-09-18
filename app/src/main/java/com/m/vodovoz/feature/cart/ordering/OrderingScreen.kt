package com.m.vodovoz.feature.cart.ordering

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.pull_to_refresh.VodovozPullToRefreshBox
import com.m.vodovoz.design_system.composables.scaffold.VodovozScaffold
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.cart.ordering.composables.OrderingBody
import com.m.vodovoz.util.extensions.deviceInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderingScreen(
    viewModel: OrderingFlowViewModel,
    viewState: OrderingFlowViewModel.OrderingState,
    scrollState: ScrollState,
) {
    val context = LocalContext.current

    VodovozScaffold(
        topBar = {
            VodovozTopBar(
                title = viewState.title,
                onBack = {
                    viewModel.navigateBack()
                }
            )
        },
        bottomBar = {
            val button = viewState.button
            BottomFloatingContainer {
                VodovozButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = button.name,
                    onClick = {
                        viewModel.doOrder(context.deviceInfo())
                    },
                    isLoading = button.loading,
                    enabled = button.enabled
                )
            }

        }
    ) { paddingValues ->
        VodovozPullToRefreshBox(
            modifier = Modifier.padding(paddingValues),
            isRefreshing = viewState.showRefreshIndicator,
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
                        }
                    )

                }

                is OrderingFlowViewModel.OrderingUiState.Success -> {}
            }
        }

    }
}