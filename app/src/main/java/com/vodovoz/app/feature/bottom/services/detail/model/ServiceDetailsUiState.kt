package com.vodovoz.app.feature.bottom.services.detail.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ServiceDetailsUiState {

    data object Loading: ServiceDetailsUiState
    data object Success: ServiceDetailsUiState
    data object Error: ServiceDetailsUiState


}