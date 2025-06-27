package com.vodovoz.app.feature.order_call_you.model

import androidx.compose.runtime.Stable

@Stable
sealed interface OrderCallYouUiState {

    data object Loading: OrderCallYouUiState
    data object CallYou: OrderCallYouUiState
    data object Error: OrderCallYouUiState


}