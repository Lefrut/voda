package com.vodovoz.app.feature.auth.recover_password.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.feature.auth.model.AuthDetailsUi
import com.vodovoz.app.feature.auth.model.AuthState

@Immutable
data class RecoverPasswordState(
    override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
    val uiState: RecoverPasswordUiState = RecoverPasswordUiState.Loading
): AuthState(authDetails)