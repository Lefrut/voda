package com.m.vodovoz.feature.auth.recover_password

import androidx.compose.runtime.Composable
import com.m.vodovoz.feature.auth.composables.AuthContent
import com.m.vodovoz.feature.auth.recover_password.model.RecoverPasswordState

@Composable
fun RecoverPasswordScreen(viewModel: RecoverPasswordViewModel, viewState: RecoverPasswordState) {
    AuthContent(
        authDetails = viewState.authDetails,
        onBackClick = { viewModel.navigateBack() },
        onFieldChange = { field, updatedField ->
            viewModel.changeField(field, updatedField)
        },
        onCheckboxChange = { checkboxUi, updatedCheckbox ->
            viewModel.changeCheckbox(checkboxUi, updatedCheckbox)
        },
        onButtonClick = { button ->
            viewModel.activateButton(button)
        },
        onHyperlinkClick = { url, title ->
            viewModel.navigateToWebView(url, title)
        }
    )
}