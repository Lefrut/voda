package com.vodovoz.app.feature.auth.login_by_phone_code

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.auth.login_by_phone_code.model.LoginByPhoneCodeEvent
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class LoginByPhoneCodeFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager

    private val viewModel: LoginByPhoneCodeViewModel by viewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.state.collectAsStateWithLifecycle()

                    LoginByPhoneCodeScreen(
                        viewState = viewState,
                        viewModel = viewModel
                    )

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                LoginByPhoneCodeEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                LoginByPhoneCodeEvent.RefreshProfile -> {
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
                            }
                        }
                    }
                }
            }
        }
    }
}
