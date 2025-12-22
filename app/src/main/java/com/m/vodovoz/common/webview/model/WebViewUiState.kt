package com.m.vodovoz.common.webview.model

import androidx.compose.runtime.Stable

@Stable
sealed interface WebViewUiState {

    data object Loading: WebViewUiState
    data object NotLoading: WebViewUiState
    data object Error: WebViewUiState


}