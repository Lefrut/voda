package com.vodovoz.app.feature.profile.change_password

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.profile.change_password.composables.ChangePasswordBody
import com.vodovoz.app.feature.profile.change_password.model.ChangePasswordState

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    viewState: ChangePasswordState,
    snackbarHostState: SnackbarHostState,
) {

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = viewState.title
            )
        },
        snackbarHost = {
            VodovozSnackbarHost(
                hostState = snackbarHostState,
            )
        },
        bottomBar = {
            VodovozButton(
                modifier = Modifier.padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                text = stringResource(R.string.change),
                isLoading = viewState.buttonLoading,
                enabled = viewState.buttonEnabled,
                onClick = {
                    viewModel.updatePassword()
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        ChangePasswordBody(
            modifier = Modifier.padding(paddingValues),
            fields = viewState.fields,
            onFieldChange = { field, updatedField ->
                viewModel.changeField(field, updatedField)
            },
            onUpdatePasswordClick = {
                viewModel.updatePassword()
            },
        )
    }
}