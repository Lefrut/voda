package com.vodovoz.app.ui.base.model

import androidx.compose.runtime.Stable

@Stable
sealed interface SplashFileState {

    data object Loading: SplashFileState
    data object Success: SplashFileState
    data object Error: SplashFileState

}