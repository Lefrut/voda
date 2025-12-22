package com.m.vodovoz.common.webview.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class WebViewState(
    val title: String = "",
    val url: String = "",
    val uiState: WebViewUiState = WebViewUiState.Loading,
    val showTopBar: Boolean = true
)
