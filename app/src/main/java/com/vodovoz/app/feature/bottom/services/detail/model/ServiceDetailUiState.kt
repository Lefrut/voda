package com.vodovoz.app.feature.bottom.services.detail.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ServiceDetailUiState {

    data object Loading: ServiceDetailUiState
    data object Success: ServiceDetailUiState
    data object Error: ServiceDetailUiState


}