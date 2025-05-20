package com.vodovoz.app.ui.base.model

sealed interface AppState {

    data object Loading: AppState
    data object ErrorLoading: AppState
    data object UserError: AppState
    data object App: AppState
    data object Blocked: AppState

}