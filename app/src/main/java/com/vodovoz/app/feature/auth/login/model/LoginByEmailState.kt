package com.vodovoz.app.feature.auth.login.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.feature.auth.login.composables.LoginByEmailUiState
import com.vodovoz.app.feature.auth.model.AuthDetailsUi
import com.vodovoz.app.feature.auth.model.AuthState

@Immutable
data class LoginByEmailState(
    val uiState: LoginByEmailUiState = LoginByEmailUiState.Loading,
    override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
) : AuthState(authDetails)
