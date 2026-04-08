package com.m.vodovoz.feature.auth.login_by_email

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToBottomTab
import com.m.vodovoz.core.navigation.navigateToRecoverPassword
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.navigation.popToProfileRoot
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.feature.auth.login.LoginFlowViewModel
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.login.model.LoginByEmailEvent
import com.m.vodovoz.feature.auth.login_by_email.api.LoginByEmailNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.delay

@Composable
fun LoginByEmailEntry(
    onRefreshAll: () -> Unit,
    navKey: LoginByEmailNavKey,
) = NavigationEntry<LoginByEmailViewModel, LoginByEmailViewModel.Factory>(
    creationCallback = { factory -> factory.create(navKey) }
) {
    val loginViewModel = when (navKey.source) {
        LoginByEmailNavKey.Source.Login -> viewModel(modelClass = LoginFlowViewModel::class)
        LoginByEmailNavKey.Source.None -> null
    }
    val viewState by viewModel.collectAsState()

    when (viewState.uiState) {
        LoginByEmailUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchLoginByEmailDetails() }
        }

        LoginByEmailUiState.Loading -> {
            LoadingPlaceholder()
        }

        LoginByEmailUiState.Success -> {
            LoginByEmailScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is LoginByEmailEvent.GoBack -> {
                    loginViewModel?.setAccountTypeById(event.selectedAccountTypeId)
                    navigator.goBack()
                }

                LoginByEmailEvent.GoToRegister -> {
                    navigator.navigateToRegister()
                }

                is LoginByEmailEvent.GoToWebView -> {
                    navigator.navigateToWebView(event.url, event.title)
                }

                LoginByEmailEvent.RefreshAll -> {
                    onRefreshAll()

                    delay(100L)

                    val redirect = viewModel.tabManager.fetchAuthRedirect()
                    if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                        navigator.popToProfileRoot()
                    } else {
                        navigator.popToProfileRoot()
                        navigator.navigateToBottomTab(redirect)
                        viewModel.tabManager.setDefaultAuthRedirect()
                    }
                }

                LoginByEmailEvent.GoToRecoverPassword -> {
                    navigator.navigateToRecoverPassword()
                }
            }
        }
    }
}
