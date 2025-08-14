package com.vodovoz.app.feature.profile

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.ProfileMainNavigator
import com.vodovoz.app.core.navigation.activate
import com.vodovoz.app.core.navigation.mainFragment
import com.vodovoz.app.core.navigation.navigateToLogin
import com.vodovoz.app.core.navigation.navigateToLoginByEmail
import com.vodovoz.app.core.navigation.navigateToUserData
import com.vodovoz.app.core.navigation.navigateToWaitFeedbackProducts
import com.vodovoz.app.core.navigation.navigateToWaterApp
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.vodovoz.app.feature.profile.navigation.ProfileChatsNavigator
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import com.vodovoz.app.ui.snackbar.snackBarHostState
import com.vodovoz.app.util.extensions.copyText
import com.vodovoz.app.util.extensions.openUrl
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
            insertVisibilityState.insertSystemBarInsets(true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.state.collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState)
                    val pullToRefreshState = rememberPullToRefreshState()

                    when (val uiState = viewState.uiState) {
                        ProfileFlowViewModel.ProfileUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        ProfileFlowViewModel.ProfileUiState.Profile -> {
                            ProfileScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                pullRefreshState = pullToRefreshState
                            )
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
                    when (events) {
                        ProfileFlowViewModel.ProfileEvents.GoToLogin -> {
                            findNavController().navigateToLogin()
                        }

                        ProfileFlowViewModel.ProfileEvents.GoToUserData -> {
                            findNavController().navigateToUserData()
                        }

                        is ProfileFlowViewModel.ProfileEvents.GoByMenuItemId -> {
                            ProfileMainNavigator.navigate(
                                id = events.itemId,
                                navController = findNavController(),
                                context = requireContext()
                            )
                        }

                        is ProfileFlowViewModel.ProfileEvents.ActivateVodovozAction -> {
                            events.action.activate(
                                navController = findNavController(),
                                context = requireActivity(),
                                cookie = cookieManager.fetchCookieSessionId() ?: "",
                                tabManager = tabManager
                            )
                        }

                        ProfileFlowViewModel.ProfileEvents.GoToLoginByEmail -> {
                            findNavController().navigateToLoginByEmail()
                        }

                        is ProfileFlowViewModel.ProfileEvents.Copy -> {
                            requireContext().copyText(events.value)
                            mainFragment?.snackBarHostState?.showSnackbar(
                                VodovozSnackBarVisuals.create(events.snackbarMessage)
                            )
                        }

                        is ProfileFlowViewModel.ProfileEvents.GoByChatItemId -> {
                            ProfileChatsNavigator.navigate(
                                chatId = events.chatId,
                                data = events.data,
                                navController = findNavController(),
                                context = requireContext()
                            )
                        }

                        ProfileFlowViewModel.ProfileEvents.GoToWaterApp -> {
                            findNavController().navigateToWaterApp()
                        }

                        ProfileFlowViewModel.ProfileEvents.GoToWaitFeedbackProducts -> {
                            findNavController().navigateToWaitFeedbackProducts()
                        }

                        is ProfileFlowViewModel.ProfileEvents.GoToWebView -> {
                            findNavController().navigateToWebView(events.url, events.title)
                        }

                        is ProfileFlowViewModel.ProfileEvents.OpenUrl -> {
                            requireContext().openUrl(events.url)
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