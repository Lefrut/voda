package com.vodovoz.app.feature.payment_method

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.payment_method.composables.PaymentMethodBody
import com.vodovoz.app.feature.payment_method.model.PaymentMethodState
import com.vodovoz.app.feature.payment_method.model.PaymentMethodUiState

@Composable
fun PaymentMethodScreen(viewModel: PaymentMethodViewModel, viewState: PaymentMethodState) {
    val uiState = viewState.uiState
    Scaffold(
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = viewState.title.ifBlank { stringResource(R.string.payment_method) }
            )
        },
        bottomBar = bottomBar@{
            if (uiState !is PaymentMethodUiState.Success) return@bottomBar

            VodovozButtonsColumn(
                modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                buttons = listOf(viewState.button),
                onButtonClick = {
                    viewModel.choosePaymentMethod()
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (uiState) {
                PaymentMethodUiState.Error -> {
                    NetworkErrorPlaceholder {
                        viewModel.fetchPaymentMethodDetails()
                    }
                }

                PaymentMethodUiState.Loading -> {
                    LoadingPlaceholder()
                }

                PaymentMethodUiState.Success -> {
                    PaymentMethodBody(
                        contentPadding = PaddingValues(
                            bottom = paddingValues.calculateBottomPadding() + 24.dp
                        ),
                        paymentSections = viewState.paymentSections,
                        onPaymentItemClick = { paymentMethod ->
                            viewModel.changePaymentMethodItem(paymentMethod)
                        }
                    )
                }
            }
        }
    }
}