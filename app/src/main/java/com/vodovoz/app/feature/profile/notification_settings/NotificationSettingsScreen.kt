package com.vodovoz.app.feature.profile.notification_settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.profile.notification_settings.composables.NotificationSettingsBody

@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    viewState: NotificationSettingsViewModel.NotSettingsState,
    snackbarHostState: SnackbarHostState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Scaffold(
            topBar = {
                VodovozTopBar(
                    onBack = { viewModel.navigateBack() },
                    title = stringResource(R.string.setting_notifications)
                )
            },
            bottomBar = bottomBar@{
                if (viewState.uiState !is NotificationSettingsViewModel.NotSettingsUiState.Success) return@bottomBar

                VodovozButtonsColumn(
                    modifier = Modifier.padding(
                        vertical = 24.dp,
                        horizontal = 16.dp
                    ),
                    buttons = listOf(viewState.button),
                    onButtonClick = { viewModel.saveNotificationSettings() }
                )
            },
            snackbarHost = {
                VodovozSnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(top = paddingValues.calculateTopPadding())
                    .animateContentSize()
                    .weight(1f)
            ) {
                when (viewState.uiState) {
                    NotificationSettingsViewModel.NotSettingsUiState.Error -> {
                        NetworkErrorPlaceholder {
                            viewModel.fetchNotificationSettingsDetails()
                        }
                    }

                    NotificationSettingsViewModel.NotSettingsUiState.Loading -> {
                        LoadingPlaceholder()
                    }

                    NotificationSettingsViewModel.NotSettingsUiState.Success -> {
                        NotificationSettingsBody(
                            modifier = Modifier,
                            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                            header = viewState.title,
                            sections = viewState.sections,
                            onWidgetChange = { widget, updatedWidget ->
                                viewModel.changeWidget(widget, updatedWidget)
                            },
                        )
                    }
                }
            }

        }

    }
}