package com.vodovoz.app.feature.profile.notificationsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.profile.notificationsettings.composables.NotificationSettingsBody

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
            title = viewState.title
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
                    switchSections = viewState.switchSections,
                    onSwitchChange = { switch, checked ->
                        viewModel.changeSwitch(switch, checked)
                    }
                )
            }
        }
    }
}