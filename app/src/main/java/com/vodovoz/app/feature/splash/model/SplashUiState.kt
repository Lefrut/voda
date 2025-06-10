package com.vodovoz.app.feature.splash.model

import androidx.compose.runtime.Stable

@Stable
sealed interface SplashUiState {

    data object Error: SplashUiState
    data object Placeholder: SplashUiState
    data object Animation: SplashUiState

}