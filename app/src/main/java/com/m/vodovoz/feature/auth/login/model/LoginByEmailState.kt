package com.m.vodovoz.feature.auth.login.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.feature.auth.model.AuthState

@Immutable
data class LoginByEmailState(
    val uiState: LoginByEmailUiState = LoginByEmailUiState.Loading,
    override val authDetails: AuthDetailsUi = AuthDetailsUi.Empty,
) : AuthState<LoginByEmailState>(authDetails) {
    override fun withAuthDetails(authDetails: AuthDetailsUi) = copy(authDetails = authDetails)
}
