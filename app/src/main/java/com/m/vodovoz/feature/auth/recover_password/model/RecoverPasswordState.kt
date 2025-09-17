package com.m.vodovoz.feature.auth.recover_password.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.AuthState

@Immutable
data class RecoverPasswordState(
    override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
    val uiState: RecoverPasswordUiState = RecoverPasswordUiState.Loading
): AuthState(authDetails)