package com.m.vodovoz.feature.payment_method

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.floating.BottomFloatingContainer
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.payment_method.composables.PaymentMethodBody
import com.m.vodovoz.feature.payment_method.model.PaymentMethodState
import com.m.vodovoz.feature.payment_method.model.PaymentMethodUiState

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
            if (uiState is PaymentMethodUiState.Success) {
                BottomFloatingContainer {
                    VodovozButtonsColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        buttons = listOf(viewState.button),
                        onButtonClick = {
                            viewModel.choosePaymentMethod()
                        }
                    )
                }
            }

        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding())) {
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
                        },
                        onFieldChange = { item, field, updatedField ->
                            viewModel.changeField(item, field, updatedField)
                        }
                    )
                }
            }
        }
    }
}