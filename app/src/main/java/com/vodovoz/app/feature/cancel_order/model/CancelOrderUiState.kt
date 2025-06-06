package com.vodovoz.app.feature.cancel_order.model

sealed interface CancelOrderUiState {

    data object Loading: CancelOrderUiState
    data object Body: CancelOrderUiState
    data object Error: CancelOrderUiState


}