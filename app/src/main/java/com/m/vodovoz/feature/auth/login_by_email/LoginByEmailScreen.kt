package com.m.vodovoz.feature.auth.login_by_email

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.auth.composables.AuthContent
import com.m.vodovoz.feature.auth.login.model.LoginByEmailState

@Composable
fun LoginByEmailScreen(
    viewModel: LoginByEmailViewModel,
    viewState: LoginByEmailState,
) {
    AuthContent(
        authDetails = viewState.authDetails,
        operations = viewModel,
    )
}
