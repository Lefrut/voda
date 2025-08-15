package com.vodovoz.app.feature.auth.reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.LifecycleStartEffect
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.auth.reg.composables.RegisterScreen
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegFlowViewModel by viewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                LifecycleStartEffect(Unit) {
                    tabManager.changeTabVisibility(false)
                    onStopOrDispose {
                        tabManager.changeTabVisibility(true)
                    }
                }

                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    
                    val snackbarHostState = remember { SnackbarHostState() }

                    RegisterScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        snackbarHostState = snackbarHostState
                    )

                    LifecycleEffect {
                        observeEvents(this, snackbarHostState)
                    }
                }
            }
        }
    }

    private suspend fun observeEvents(
        uiCoroutinesScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
    ) {
        viewModel.events.collect { event ->
            when (event) {
                is RegFlowViewModel.RegEvents.ShowSnackbar -> {
                    uiCoroutinesScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                RegFlowViewModel.RegEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                RegFlowViewModel.RegEvents.GoToProfile -> {
                    profileViewModel.fetchProfileDetails()
                    findNavController().popBackStack(
                        R.id.profileFragment,
                        false
                    )
                }

                is RegFlowViewModel.RegEvents.GoToWebView -> {
                    findNavController().navigateToWebView(event.url, event.title)
                }

                RegFlowViewModel.RegEvents.GoToLogin -> {
                    findNavController().navigate(
                        R.id.loginFragment,
                        bundleOf(),
                        NavOptions.Builder().setPopUpTo(R.id.profileFragment, false).build()
                    )
                }

                RegFlowViewModel.RegEvents.GoToLoginByEmail -> {
                    findNavController().navigate(
                        R.id.loginByEmailFragment,
                        bundleOf(),
                        NavOptions.Builder().setPopUpTo(R.id.profileFragment, false).build()
                    )
                }

                RegFlowViewModel.RegEvents.RefreshAll -> {
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
                        tabManager.selectTab(redirect)
                        tabManager.setDefaultAuthRedirect()
                    }
                }
            }
        }

    }

}