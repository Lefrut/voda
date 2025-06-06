package com.vodovoz.app.feature.cart.ordering

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.cart.ordering.composables.OrderingBody

@Composable
fun OrderingScreen(
    viewModel: OrderingFlowViewModel,
    viewState: OrderingFlowViewModel.OrderingState,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = {
                viewModel.navigateBack()
            },
            title = viewState.title.ifEmpty { stringResource(id = R.string.ordering_title_text) }
        )
        when (viewState.uiState) {
            OrderingFlowViewModel.OrderingUiState.Error -> {
                NetworkErrorPlaceholder {
                    viewModel.fetchOrderingDetails()
                }
            }

            OrderingFlowViewModel.OrderingUiState.Loading -> {
                LoadingPlaceholder()
            }

            OrderingFlowViewModel.OrderingUiState.Success -> {
                OrderingBody(
                    modifier = Modifier.weight(1f),
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
                        viewModel.doOrder()
                    }
                )

            }
        }
    }
}