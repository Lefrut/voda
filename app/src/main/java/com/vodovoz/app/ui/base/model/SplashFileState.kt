package com.vodovoz.app.ui.base.model

sealed interface SplashFileState {

    data object Loading: SplashFileState
    data object Success: SplashFileState
    data object Error: SplashFileState
}