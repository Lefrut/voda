package com.m.vodovoz.feature.profile

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.ProfileMainNavigator
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.mainFragment
import com.m.vodovoz.core.navigation.navigateToLogin
import com.m.vodovoz.core.navigation.navigateToLoginByEmail
import com.m.vodovoz.core.navigation.navigateToOrderDetails
import com.m.vodovoz.core.navigation.navigateToOrdersHistory
import com.m.vodovoz.core.navigation.navigateToQuestionnaires
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToUserData
import com.m.vodovoz.core.navigation.navigateToWaitFeedbackProducts
import com.m.vodovoz.core.navigation.navigateToWaterApp
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.profile.navigation.ProfileChatsNavigator
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.snackbar.snackBarHostState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    internal val viewModel: ProfileFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeEvents()
        observeTabReselect()
    }

    @Inject
    lateinit var insertVisibilityState: InsetsVisibilityState

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            delay(300)
            insertVisibilityState.consumeSystemBarInsets(true)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()

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
                }
            }
        }
    }


    private fun observeEvents() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events
                .collect { events ->
                    with(findNavController()) {
                        when (events) {
                            ProfileFlowViewModel.ProfileEvents.GoToLogin -> {
                                navigateToLogin()

                            }

                            ProfileFlowViewModel.ProfileEvents.GoToUserData -> {
                                navigateToUserData()
                            }

                            is ProfileFlowViewModel.ProfileEvents.GoByMenuItemId -> {
                                ProfileMainNavigator.navigate(
                                    id = events.itemId,
                                    navController = this,
                                )
                            }

                            is ProfileFlowViewModel.ProfileEvents.ActivateVodovozAction -> {
                                events.action.activate(
                                    navController = this,
                                    context = requireActivity(),
                                    cookie = cookieManager.fetchCookieSessionId() ?: "",
                                    tabManager = tabManager
                                )
                            }

                            ProfileFlowViewModel.ProfileEvents.GoToLoginByEmail -> {
                                navigateToLoginByEmail()
                            }

                            is ProfileFlowViewModel.ProfileEvents.Copy -> {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    requireContext().copyText(events.value)
                                    mainFragment?.snackBarHostState?.showSnackbar(
                                        VodovozSnackBarVisuals.create(events.snackbarMessage)
                                    )
                                }

                            }

                            is ProfileFlowViewModel.ProfileEvents.GoByChatItemId -> {
                                viewModel.closeSupportingBottomSheet()
                                ProfileChatsNavigator.navigate(
                                    chatId = events.chatId,
                                    data = events.data,
                                    navController = this,
                                    context = requireContext()
                                )
                            }

                            ProfileFlowViewModel.ProfileEvents.GoToWaterApp -> {
                                navigateToWaterApp()
                            }

                            ProfileFlowViewModel.ProfileEvents.GoToWaitFeedbackProducts -> {
                                navigateToWaitFeedbackProducts()
                            }

                            is ProfileFlowViewModel.ProfileEvents.GoToWebView -> {
                                navigateToWebView(events.url, events.title)
                            }

                            is ProfileFlowViewModel.ProfileEvents.OpenUrl -> {
                                requireContext().openUrl(events.url)
                            }

                            ProfileFlowViewModel.ProfileEvents.DoNothing -> {}
                            is ProfileFlowViewModel.ProfileEvents.GoToOrderDetails -> {
                                navigateToOrderDetails(events.orderId)
                            }

                            ProfileFlowViewModel.ProfileEvents.GoToOrders -> {
                                navigateToOrdersHistory()
                            }

                            ProfileFlowViewModel.ProfileEvents.GoToQuestionnaires -> {
                                navigateToQuestionnaires()
                            }
                        }

                    }
                }
        }
    }


    private fun observeTabReselect() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            tabManager.observeTabReselect()
                .collect {
                    if (it != TabManager.DEFAULT_STATE && it == R.id.profileFragment) {
                        tabManager.setDefaultState()
                    }
                }

        }
    }

}