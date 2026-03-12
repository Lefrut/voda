package com.m.vodovoz.feature.profile.user_data

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToImagePicker
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.main.BottomNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserDataEntry(
    onRefreshAll: () -> Unit,
    onUpdateProfile: () -> Unit,
) = NavigationEntry<UserDataFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleStartEffect(Unit) {
        viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
        onStopOrDispose { }
    }

    when (viewState.uiState) {
        UserDataFlowViewModel.UserDataUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchUserData() }
        }

        UserDataFlowViewModel.UserDataUiState.Loading -> {
            LoadingPlaceholder()
        }

        UserDataFlowViewModel.UserDataUiState.Success -> {
            UserDataScreen(
                viewModel = viewModel,
                viewState = viewState,
                snackbarHostState = snackbarHostState
            )
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                UserDataFlowViewModel.UserDataEvents.RefreshAllAndGoBack -> {
                    onRefreshAll()

                    delay(200)

                    navigator.navigate(BottomNavKey.Profile)
                }

                UserDataFlowViewModel.UserDataEvents.UpdateProfile -> {
                    onUpdateProfile()
                }

                UserDataFlowViewModel.UserDataEvents.GoBack -> {
                    navigator.goBack()
                }

                is UserDataFlowViewModel.UserDataEvents.ShowSnackbar -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                UserDataFlowViewModel.UserDataEvents.OpenImagePicker -> {
                    navigator.navigateToImagePicker()
                }
            }
        }
    }
}
