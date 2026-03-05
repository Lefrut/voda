package com.m.vodovoz.feature.auth.login_by_email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.core.navigation.navigateToRecoverPassword
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.login.composables.LoginByEmailUiState
import com.m.vodovoz.feature.auth.login.model.LoginByEmailEvent
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class LoginByEmailFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    private val viewModel: LoginByEmailViewModel by viewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
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
                                    val navController = findNavController()
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set(AuthArgs.ACCOUNT_TYPE_ID, event.selectedAccountTypeId)
                                    navController.popBackStack()
                                }

                                LoginByEmailEvent.GoToRegister -> {
                                    findNavController().navigateToRegister()
                                }

                                is LoginByEmailEvent.GoToWebView -> {
                                    findNavController().navigateToWebView(event.url, event.title)
                                }

                                LoginByEmailEvent.RefreshAll -> {
                                    profileViewModel.refresh()
                                    homeViewModel.refresh()
                                    cartFlowViewModel.refresh()
                                    favoriteViewModel.refresh()

                                    delay(100L)

                                    val redirect = tabManager.fetchAuthRedirect()
                                    if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                                        findNavController().popBackStack(
                                            R.id.profileFragment, false
                                        )
                                    } else {
                                        findNavController().popBackStack(
                                            R.id.profileFragment, false
                                        )
                                        tabManager.selectTab(redirect)
                                        tabManager.setDefaultAuthRedirect()
                                    }
                                }

                                LoginByEmailEvent.GoToRecoverPassword -> {
                                    findNavController().navigateToRecoverPassword()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
