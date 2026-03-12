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
        val accountTypeId = navigator.currentBackStackEntry
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
                    navigator.goBack()
                }

                is LoginFlowViewModel.LoginEvents.GoToWebView -> {
                    navigator.navigateToWebView(
                        url = event.url,
                        title = event.title,
                    )
                }

                is LoginFlowViewModel.LoginEvents.GoToLoginByEmail -> {
                    navigator.navigateToLoginByEmail(event.selectedAccountTypeId)
                }

                LoginFlowViewModel.LoginEvents.GoToRegister -> {
                    navigator.navigateToRegister()
                }

                is LoginFlowViewModel.LoginEvents.GoToLoginByPhone -> {
                    navigator.navigateToLoginByPhone(
                        phone = event.phone,
                        waitSeconds = event.waitSeconds,
                        userUrl = event.userUrl
                    )
                }
            }
        }
    }
}
