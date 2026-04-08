package com.m.vodovoz.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.ProfileMainNavigator
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToLogin
import com.m.vodovoz.core.navigation.navigateToLoginByEmail
import com.m.vodovoz.core.navigation.navigateToOrderDetails
import com.m.vodovoz.core.navigation.navigateToOrdersHistory
import com.m.vodovoz.core.navigation.navigateToQuestionnaires
import com.m.vodovoz.core.navigation.navigateToUserData
import com.m.vodovoz.core.navigation.navigateToWaitFeedbackProducts
import com.m.vodovoz.core.navigation.navigateToWaterApp
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.main.BottomNavKey
import com.m.vodovoz.feature.profile.navigation.ProfileChatsNavigator
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.mvi.collectEvents
import com.m.vodovoz.ui.snackbar.snackBarHostState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.delay

@Composable
fun ProfileEntry(
    viewModel: ProfileFlowViewModel,
) {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val tabManager = viewModel.tabManager
    val insetsVisibilityState = viewModel.insetsVisibilityState

    LifecycleEffect(tabManager) {
        tabManager.observeTabReselect().collect {
            if (it == BottomNavKey.Profile) {
                tabManager.resetTabReselect()
            }
        }
    }

    LifecycleEffect(insetsVisibilityState) {
        delay(300)
        insetsVisibilityState.consumeSystemBarInsets(true)
    }

    when (val uiState = viewState.uiState) {
        ProfileFlowViewModel.ProfileUiState.Loading -> {
            LoadingPlaceholder()
        }

        ProfileFlowViewModel.ProfileUiState.Profile -> {
            ProfileScreen(
                viewModel = viewModel,
                viewState = viewState,
            )

            LifecycleEffect {
                viewModel.pendingDeeplinkFlow.collect {}
            }
        }

        is ProfileFlowViewModel.ProfileUiState.UserNotFound -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .systemBarsPadding()
            ) {
                Text(
                    text = uiState.placeholder.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(16.dp)
                )
                VodovozPlaceholder(
                    data = uiState.placeholder,
                    onButtonClick = {
                        viewModel.navigateToLoginOrRegister()
                    }
                )
            }
        }

        ProfileFlowViewModel.ProfileUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchProfileDetails() }
        }
    }

    viewModel.collectEvents { events ->
        when (events) {
            ProfileFlowViewModel.ProfileEvents.GoToLogin -> {
                navigator.navigateToLogin()
            }

            ProfileFlowViewModel.ProfileEvents.GoToUserData -> {
                navigator.navigateToUserData()
            }

            is ProfileFlowViewModel.ProfileEvents.GoByMenuItemId -> {
                ProfileMainNavigator.navigate(
                    id = events.itemId,
                    navigator = navigator,
                )
            }

            is ProfileFlowViewModel.ProfileEvents.ActivateVodovozAction -> {
                events.action.activate(
                    navigator = navigator,
                    context = context,
                    cookie = viewModel.cookieManager.fetchCookieSessionId().orEmpty(),
                    tabManager = tabManager
                )
            }

            ProfileFlowViewModel.ProfileEvents.GoToLoginByEmail -> {
                navigator.navigateToLoginByEmail()
            }

            is ProfileFlowViewModel.ProfileEvents.Copy -> {
                context.copyText(events.value)
                val hostFragment = (context as? FragmentActivity)
                    ?.supportFragmentManager
                    ?.findFragmentById(R.id.fcvMainContainer)
                    ?.childFragmentManager
                    ?.fragments
                    ?.lastOrNull()

                hostFragment?.snackBarHostState?.showSnackbar(
                    VodovozSnackBarVisuals.create(events.snackbarMessage)
                )
            }

            is ProfileFlowViewModel.ProfileEvents.GoByChatItemId -> {
                viewModel.closeSupportingBottomSheet()
                ProfileChatsNavigator.navigate(
                    chatId = events.chatId,
                    data = events.data,
                    navigator = navigator,
                    context = context
                )
            }

            ProfileFlowViewModel.ProfileEvents.GoToWaterApp -> {
                navigator.navigateToWaterApp()
            }

            ProfileFlowViewModel.ProfileEvents.GoToWaitFeedbackProducts -> {
                navigator.navigateToWaitFeedbackProducts()
            }

            is ProfileFlowViewModel.ProfileEvents.GoToWebView -> {
                navigator.navigateToWebView(events.url, events.title)
            }

            is ProfileFlowViewModel.ProfileEvents.OpenUrl -> {
                context.openUrl(events.url)
            }

            ProfileFlowViewModel.ProfileEvents.DoNothing -> {}
            is ProfileFlowViewModel.ProfileEvents.GoToOrderDetails -> {
                navigator.navigateToOrderDetails(events.orderId)
            }

            ProfileFlowViewModel.ProfileEvents.GoToOrders -> {
                navigator.navigateToOrdersHistory()
            }

            ProfileFlowViewModel.ProfileEvents.GoToQuestionnaires -> {
                navigator.navigateToQuestionnaires()
            }
        }
    }

}
