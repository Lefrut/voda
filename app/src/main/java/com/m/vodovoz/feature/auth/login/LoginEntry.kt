package com.m.vodovoz.feature.auth.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.core.navigation.navigateToLoginByEmail
import com.m.vodovoz.core.navigation.navigateToLoginByPhone
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun LoginEntry() = NavigationEntry<LoginFlowViewModel> {
    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        val accountTypeId = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<String>(AuthArgs.ACCOUNT_TYPE_ID)
        viewModel.setAccountTypeById(accountTypeId)
        onStopOrDispose { }
    }

    when (viewState.uiState) {
        LoginFlowViewModel.LoginUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchLoginDetails() }
        }

        LoginFlowViewModel.LoginUiState.Loading -> {
            LoadingPlaceholder()
        }

        LoginFlowViewModel.LoginUiState.Success -> {
            LoginScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                LoginFlowViewModel.LoginEvents.GoBack -> {
                    navController.popBackStack()
                }

                is LoginFlowViewModel.LoginEvents.GoToWebView -> {
                    navController.navigateToWebView(
                        url = event.url,
                        title = event.title,
                    )
                }

                is LoginFlowViewModel.LoginEvents.GoToLoginByEmail -> {
                    navController.navigateToLoginByEmail(event.selectedAccountTypeId)
                }

                LoginFlowViewModel.LoginEvents.GoToRegister -> {
                    navController.navigateToRegister()
                }

                is LoginFlowViewModel.LoginEvents.GoToLoginByPhone -> {
                    navController.navigateToLoginByPhone(
                        phone = event.phone,
                        waitSeconds = event.waitSeconds,
                        userUrl = event.userUrl
                    )
                }
            }
        }
    }
}
