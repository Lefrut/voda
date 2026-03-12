package com.m.vodovoz.feature.profile.notification_settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.launch

@Composable
fun NotificationSettingsEntry() = NavigationEntry<NotificationSettingsViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    NotificationSettingsScreen(
        viewModel = viewModel,
        viewState = viewState,
        snackbarHostState = snackbarHostState
    )

    LifecycleEffect(snackbarHostState) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationSettingsViewModel.NotSettingsEvents.ShowToast -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                NotificationSettingsViewModel.NotSettingsEvents.GoBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}
