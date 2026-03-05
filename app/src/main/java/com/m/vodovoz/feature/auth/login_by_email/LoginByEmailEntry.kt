package com.m.vodovoz.feature.auth.login_by_email

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToRecoverPassword
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.login.model.LoginByEmailEvent
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.delay

@Composable
fun LoginByEmailEntry(
    onRefreshAll: () -> Unit,
) = NavigationEntry<LoginByEmailViewModel> {
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
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AuthArgs.ACCOUNT_TYPE_ID, event.selectedAccountTypeId)
                    navController.popBackStack()
                }

                LoginByEmailEvent.GoToRegister -> {
                    navController.navigateToRegister()
                }

                is LoginByEmailEvent.GoToWebView -> {
                    navController.navigateToWebView(event.url, event.title)
                }

                LoginByEmailEvent.RefreshAll -> {
                    onRefreshAll()

                    delay(100L)

                    val redirect = viewModel.tabManager.fetchAuthRedirect()
                    if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                        navController.popBackStack(R.id.profileFragment, false)
                    } else {
                        navController.popBackStack(R.id.profileFragment, false)
                        viewModel.tabManager.selectTab(redirect)
                        viewModel.tabManager.setDefaultAuthRedirect()
                    }
                }

                LoginByEmailEvent.GoToRecoverPassword -> {
                    navController.navigateToRecoverPassword()
                }
            }
        }
    }
}
