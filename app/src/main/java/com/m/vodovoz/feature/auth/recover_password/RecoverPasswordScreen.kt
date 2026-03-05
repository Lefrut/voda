package com.m.vodovoz.feature.auth.recover_password

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.auth.composables.AuthContent
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordState

@Composable
fun RecoverPasswordScreen(viewModel: RecoverPasswordViewModel, viewState: RecoverPasswordState) {
    AuthContent(
        authDetails = viewState.authDetails,
        operations = viewModel,
    )
}
