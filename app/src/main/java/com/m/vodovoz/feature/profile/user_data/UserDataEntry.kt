package com.m.vodovoz.feature.profile.user_data

import android.os.Bundle
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.findRootNavController
import com.m.vodovoz.core.navigation.slideAnim
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
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
    val view = LocalView.current

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

                    navController.navigate(
                        resId = R.id.profileFragment,
                        args = Bundle.EMPTY,
                        navOptions = navOptions {
                            popUpTo(R.id.profileFragment) { inclusive = true }
                            anim {
                                enter = R.anim.fade_in
                                exit = R.anim.fade_out
                            }
                        }
                    )
                }

                UserDataFlowViewModel.UserDataEvents.UpdateProfile -> {
                    onUpdateProfile()
                }

                UserDataFlowViewModel.UserDataEvents.GoBack -> {
                    navController.popBackStack()
                }

                is UserDataFlowViewModel.UserDataEvents.ShowSnackbar -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                UserDataFlowViewModel.UserDataEvents.OpenImagePicker -> {
                    view.findRootNavController()?.navigate(
                        R.id.imagePickerFragment,
                        Bundle.EMPTY,
                        navOptions { slideAnim() }
                    )
                }
            }
        }
    }
}
