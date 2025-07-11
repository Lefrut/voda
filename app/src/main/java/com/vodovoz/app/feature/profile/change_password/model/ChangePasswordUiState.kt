package com.vodovoz.app.feature.profile.change_password.model

import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface ChangePasswordUiState {
    data class Placeholder(val placeholder: VodovozPlaceholderUi) :
        ChangePasswordUiState

    data object Loading: ChangePasswordUiState
    data object ChangePassword: ChangePasswordUiState

}