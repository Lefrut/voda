package com.m.vodovoz.feature.splash.model

sealed interface SplashEvent {

    data object RefreshApp: SplashEvent
    data object HideAndroidSplash : SplashEvent
}