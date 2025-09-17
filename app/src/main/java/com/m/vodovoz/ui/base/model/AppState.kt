package com.m.vodovoz.ui.base.model

import androidx.compose.runtime.Stable

@Stable
sealed interface AppState {

    data object Loading: AppState
    data object ErrorLoading: AppState
    data object UserError: AppState
    data object App: AppState
    data object Blocked: AppState

}