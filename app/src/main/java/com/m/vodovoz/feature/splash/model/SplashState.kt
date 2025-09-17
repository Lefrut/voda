package com.m.vodovoz.feature.splash.model

import androidx.compose.runtime.Immutable

@Immutable
data class SplashState(
    val uiState: SplashUiState = SplashUiState.Placeholder,
    val filePath: String = "",
)
