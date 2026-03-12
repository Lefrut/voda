package com.m.vodovoz.feature.auth.login_by_phone_code

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
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.login_by_phone_code.model.LoginByPhoneCodeEvent
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.main.AppNavigatorStore
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
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
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
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
                    val viewState by viewModel.collectAsState()

                    LoginByPhoneCodeScreen(
                        viewState = viewState,
                        viewModel = viewModel
                    )

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            val navigator = AppNavigatorStore.navigator ?: return@collect
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
                                        navigator.popBackStack(
                                            R.id.profileFragment, false
                                        )
                                    } else {
                                        navigator.popBackStack(
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
