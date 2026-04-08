package com.m.vodovoz.feature.auth.login_by_phone_code

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.ViewModelStoreOwner
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToBottomTab
import com.m.vodovoz.core.navigation.popToProfileRoot
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.login_by_phone_code.api.LoginByPhoneCodeNavKey
import com.m.vodovoz.feature.auth.login_by_phone_code.model.LoginByPhoneCodeEvent
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.mvi.collectAsState
import kotlinx.coroutines.delay

@Composable
fun LoginByPhoneCodeEntry(
    navKey: LoginByPhoneCodeNavKey,
) = NavigationEntry<LoginByPhoneCodeViewModel, LoginByPhoneCodeViewModel.Factory>(
    creationCallback = { factory -> factory.create(navKey) }
) {
    val activity = LocalActivity.current ?: return@NavigationEntry
    val owner = activity as? ViewModelStoreOwner ?: return@NavigationEntry
    val profileViewModel = hiltViewModel<ProfileFlowViewModel>(owner)
    val homeViewModel = hiltViewModel<HomeFlowViewModel>(owner)
    val cartFlowViewModel = hiltViewModel<CartFlowViewModel>(owner)
    val favoriteViewModel = hiltViewModel<FavoriteFlowViewModel>(owner)
    val tabManager: TabManager = profileViewModel.tabManager

    val viewState by viewModel.collectAsState()

    LifecycleStartEffect(Unit) {
        tabManager.setTabVisibility(false)
        onStopOrDispose {
            tabManager.setTabVisibility(true)
        }
    }

    LoginByPhoneCodeScreen(
        viewState = viewState,
        viewModel = viewModel
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                LoginByPhoneCodeEvent.GoBack -> {
                    navigator.goBack()
                }

                LoginByPhoneCodeEvent.RefreshProfile -> {
                    profileViewModel.refresh()
                    homeViewModel.refresh()
                    cartFlowViewModel.refresh()
                    favoriteViewModel.refresh()

                    delay(100L)

                    val redirect = tabManager.fetchAuthRedirect()
                    if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                        navigator.popToProfileRoot()
                    } else {
                        navigator.popToProfileRoot()
                        navigator.navigateToBottomTab(redirect)
                        tabManager.setDefaultAuthRedirect()
                    }
                }
            }
        }
    }
}
