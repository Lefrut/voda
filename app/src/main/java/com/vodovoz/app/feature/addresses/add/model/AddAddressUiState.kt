package com.vodovoz.app.feature.addresses.add.model

import androidx.compose.runtime.Stable

@Stable
sealed interface AddAddressUiState {

    data object Form: AddAddressUiState
    data object Loading: AddAddressUiState
    data object Error: AddAddressUiState


}