package com.m.vodovoz.feature.profile.change_password.model

sealed interface ChangePasswordEvent {
    data object GoBack : ChangePasswordEvent
    data class ShowSnackbar(val message: String) : ChangePasswordEvent
}