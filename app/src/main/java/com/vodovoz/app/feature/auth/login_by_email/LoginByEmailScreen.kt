package com.vodovoz.app.feature.auth.login_by_email

import androidx.compose.runtime.Composable
import com.vodovoz.app.feature.auth.composables.AuthContent
import com.vodovoz.app.feature.auth.login.model.LoginByEmailState

@Composable
fun LoginByEmailScreen(
    viewModel: LoginByEmailViewModel,
    viewState: LoginByEmailState,
) {
    AuthContent(
        authDetails = viewState.authDetails,
        onBackClick = { viewModel.navigateBack() },
        onFieldChange = { field, updatedField ->
            viewModel.changeField(field, updatedField)
        },
        onCheckboxChange = { checkbox, updatedCheckbox ->
            viewModel.changeCheckbox(checkbox, updatedCheckbox)
        },
        onButtonClick = { button ->
            viewModel.activateButton(button)
        },
        onHyperlinkClick = { url, urlIndex ->
          viewModel.openAgreementUrl(url, urlIndex)
        },
        onForgotPasswordClick = {
            viewModel.navigateToRecoveryPassword()
        }
    )
}