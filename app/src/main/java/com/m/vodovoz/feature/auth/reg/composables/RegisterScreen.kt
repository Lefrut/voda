package com.m.vodovoz.feature.auth.reg.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.feature.auth.composables.AuthContent
import com.m.vodovoz.feature.auth.reg.RegFlowViewModel

@Composable
fun RegisterScreen(
    viewModel: RegFlowViewModel,
    viewState: RegFlowViewModel.RegState,
    snackbarHostState: SnackbarHostState,
) {
    Column {
        AuthContent(
            modifier = Modifier.weight(1f),
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
            onHyperlinkClick = { url, title ->
                viewModel.navigateToWebView(url, title)
            }
        )
        VodovozSnackbarHost(
            hostState = snackbarHostState,
        )
    }
}