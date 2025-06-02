package com.vodovoz.app.feature.profile.notification_settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.profile.notification_settings.composables.NotificationSettingsBody

@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel,
    viewState: NotificationSettingsViewModel.NotSettingsState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = stringResource(R.string.setting_notifications)
        )
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
                    header = viewState.title,
                    sections = viewState.sections,
                    button = viewState.button,
                    onWidgetChange = { widget, updatedWidget ->
                        viewModel.changeWidget(widget, updatedWidget)
                    },
                    onSaveClick = {
                        viewModel.saveNotificationSettings()
                    }
                )
            }
        }
    }
}