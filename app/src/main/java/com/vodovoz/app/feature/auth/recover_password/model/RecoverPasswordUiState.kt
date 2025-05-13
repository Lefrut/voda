package com.vodovoz.app.feature.auth.recover_password.model

import com.vodovoz.app.design_system.model.VodovozPlaceholderUi

sealed interface RecoverPasswordUiState {

    data object Error : RecoverPasswordUiState
    data object Loading : RecoverPasswordUiState
    data object Body : RecoverPasswordUiState
    data class Success(val placeholder: VodovozPlaceholderUi) : RecoverPasswordUiState

}