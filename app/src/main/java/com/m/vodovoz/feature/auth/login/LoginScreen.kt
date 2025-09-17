package com.m.vodovoz.feature.auth.login

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.auth.composables.AuthContent

@Composable
fun LoginScreen(viewModel: LoginFlowViewModel, viewState: LoginFlowViewModel.LoginState) {
    AuthContent(
        authDetails = viewState.authDetails,
        onBackClick = { viewModel.navigateBack() },
        onFieldChange = { field, updatedField ->
            viewModel.changeField(field, updatedField)
        },
        onButtonClick = { button ->
            viewModel.activateButton(button)
        },
        onHyperlinkClick = { url, title ->
            viewModel.openUrl(url, title)
        },
        onCheckboxChange = { checkbox, updatedCheckbox ->
            viewModel.changeCheckbox(checkbox, updatedCheckbox)
        },

    )
}