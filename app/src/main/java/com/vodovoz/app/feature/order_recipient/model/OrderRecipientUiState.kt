package com.vodovoz.app.feature.order_recipient.model

import androidx.compose.runtime.Stable

@Stable
sealed interface OrderRecipientUiState {

    data object Loading: OrderRecipientUiState
    data object Recipient: OrderRecipientUiState
    data object Error: OrderRecipientUiState

}