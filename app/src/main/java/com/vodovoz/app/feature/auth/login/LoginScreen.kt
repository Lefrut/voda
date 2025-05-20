package com.vodovoz.app.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.auth.login.composables.LoginBody

@Suppress("NonSkippableComposable")
@Composable
fun LoginScreen(viewModel: LoginFlowViewModel, viewState: LoginFlowViewModel.LoginState) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize()
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = viewState.title
        )
        LoginBody(
            fields = viewState.fields,
            description = viewState.description,
            showAgreements = viewState.showAgreements,
            agreementTextHtml = viewState.agreementTextHtml,
            agreementChecked = viewState.agreementChecked,
            subscribeChecked = viewState.subscribeChecked,
            showRegisterText = viewState.showRegisterText,
            buttons = viewState.buttons,
            errorText = viewState.errorText,
            onButtonClick = { button ->
                viewModel.activateButton(button)
            },
            onFieldChange = { field, updatedField ->
                viewModel.changeField(field, updatedField)
            },
            onAgreementCheck = {
                viewModel.checkAgreement(it)
            },
            onHyperlinkClick = { url, index ->
                viewModel.openAgreementUrl(url, index)
            },
            onSubscribeCheck = {
                viewModel.checkSubscribe(it)
            },
            onRegisterTextClick = {
                viewModel.navigateToRegister()
            }
        )
    }
}