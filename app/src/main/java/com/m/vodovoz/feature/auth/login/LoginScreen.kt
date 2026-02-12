package com.m.vodovoz.feature.auth.login

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.auth.composables.AuthContent

@Composable
fun LoginScreen(viewModel: LoginFlowViewModel, viewState: LoginFlowViewModel.LoginState) {
    AuthContent(
        authDetails = viewState.authDetails,
        operations = viewModel,
    )
}
