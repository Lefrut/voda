package com.m.vodovoz.feature.payment_method.model

import androidx.compose.runtime.Stable

@Stable
sealed interface PaymentMethodUiState {

    data object Error: PaymentMethodUiState
    data object Loading: PaymentMethodUiState
    data object Success: PaymentMethodUiState

}