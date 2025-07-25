package com.vodovoz.app.feature.auth.recover_password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.auth.recover_password.composables.RecoverPasswordBody
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordState

@Composable
fun RecoverPasswordScreen(viewModel: RecoverPasswordViewModel, viewState: RecoverPasswordState) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            title = viewState.title.ifEmpty {
                stringResource(R.string.recover_password)
            },
            onBack = {
                viewModel.navigateBack()
            }
        )
        RecoverPasswordBody(
            description = viewState.description,
            fields = viewState.fields,
            buttons = viewState.buttons,
            showAgreements = viewState.showAgreement,
            agreementChecked = viewState.agreementChecked,
            agreementHtml = viewState.agreementHtml,
            onAgreementCheck = { newValue ->
                viewModel.checkAgreement(newValue)
            },
            onHyperlinkClick = { url, urlIndex ->
                viewModel.navigateToWebView(url, urlIndex)
            },
            onButtonClick = { button ->
                viewModel.activateButton(button)
            },
            onFieldChange = { field, updatedField ->
                viewModel.changeField(field, updatedField)
            }
        )
    }
}