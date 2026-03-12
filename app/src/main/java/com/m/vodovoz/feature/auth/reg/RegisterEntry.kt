package com.m.vodovoz.feature.auth.reg

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.reg.composables.RegisterScreen
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterEntry(
    onRefreshAll: () -> Unit,
    onFetchProfile: () -> Unit,
) = NavigationEntry<RegFlowViewModel> {
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    RegisterScreen(
        viewModel = viewModel,
        viewState = viewState,
        snackbarHostState = snackbarHostState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is RegFlowViewModel.RegEvents.ShowSnackbar -> {
                    launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                RegFlowViewModel.RegEvents.GoBack -> {
                    navigator.goBack()
                }

                RegFlowViewModel.RegEvents.GoToProfile -> {
                    onFetchProfile()
                    navigator.popBackStack(
                        R.id.profileFragment,
                        false
                    )
                }

                is RegFlowViewModel.RegEvents.GoToWebView -> {
                    navigator.navigateToWebView(event.url, event.title)
                }

                RegFlowViewModel.RegEvents.GoToLogin -> {
                    navigator.navigate(
                        R.id.loginFragment,
                        bundleOf(),
                        NavOptions.Builder().setPopUpTo(R.id.profileFragment, false).build()
                    )
                }

                RegFlowViewModel.RegEvents.GoToLoginByEmail -> {
                    navigator.navigate(
                        R.id.loginByEmailFragment,
                        bundleOf(),
                        NavOptions.Builder().setPopUpTo(R.id.profileFragment, false).build()
                    )
                }

                RegFlowViewModel.RegEvents.RefreshAll -> {
                    onRefreshAll()

                    delay(100L)

                    val redirect = viewModel.tabManager.fetchAuthRedirect()
                    if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                        navigator.popBackStack(
                            R.id.profileFragment,
                            false
                        )
                    } else {
                        viewModel.tabManager.selectTab(redirect)
                        viewModel.tabManager.setDefaultAuthRedirect()
                    }
                }
            }
        }
    }
}
