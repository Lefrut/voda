package com.vodovoz.app.feature.delivery_date.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed interface DeliveryDateUiState {

    data object Loading: DeliveryDateUiState
    data object Success: DeliveryDateUiState
    data object Error: DeliveryDateUiState
    data object BodyLoading: DeliveryDateUiState

}