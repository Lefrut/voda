package com.m.vodovoz.feature.certificate_activation.model

import androidx.compose.runtime.Stable

@Stable
sealed interface CertificateActivationUiState {

    data object Loading: CertificateActivationUiState
    data object Details: CertificateActivationUiState
    data object Error: CertificateActivationUiState
    data class CertificateActivated(val message: String) : CertificateActivationUiState

}