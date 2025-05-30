package com.vodovoz.app.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.catalog.CatalogFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.feature.splash.model.SplashEvent
import com.vodovoz.app.ui.base.MainActivityViewModel
import com.vodovoz.app.ui.base.SplashFileViewModel
import com.vodovoz.app.ui.base.model.AppState
import com.vodovoz.app.ui.base.model.SplashFileState
import com.vodovoz.app.util.SplashFileConfig
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.disableFullScreen
import com.vodovoz.app.util.extensions.enableFullScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {


    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val splashViewModel: SplashViewModel by activityViewModels()
    private val splashFileViewModel: SplashFileViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val catalogViewModel: CatalogFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()


    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var siteStateManager: SiteStateManager

    override fun onStart() {
        super.onStart()
        requireActivity().enableFullScreen()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().disableFullScreen()
        activityViewModel.finishAndroidSplash()
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
                    val viewState by splashViewModel.state.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    AppSplashScreen(
                        viewModel = splashViewModel,
                        viewState = viewState
                    )

                    LifecycleEffect {
                        splashFileViewModel.fileState.collectLatest { fileState ->
                            when(fileState){
                                SplashFileState.Error -> {
                                    splashViewModel.hideAndroidSplash()
                                }
                                SplashFileState.Success -> {
                                    splashViewModel.changeToAnimation(SplashFileConfig.getSplashFile(context))
                                }
                                SplashFileState.Loading -> {}
                            }
                        }
                    }

                    LifecycleEffect {
                        listenEvents()
                    }

                    LifecycleEffect {
                        listenAppState()
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountManager.reportEvent("Зашел в приложение")
        handlePushData()
    }

    private suspend fun listenEvents(): Unit = splashViewModel.events.collect { event ->
        when (event) {
            SplashEvent.RefreshApp -> {
                activityViewModel.checkAppState()
            }

            SplashEvent.HideAndroidSplash -> {
                activityViewModel.finishAndroidSplash()
            }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun listenAppState() =
        activityViewModel.appState.debounce(1L).collect { appState ->

            val navController = findNavController()
            val androidSplash = activityViewModel.androidSplash.value

            when (appState) {
                AppState.App -> {
                    fetchDataForScreens().join()
                    navController.navigateToScreen(
                        R.id.mainFragment,
                    )
                }

                AppState.Blocked -> {
                    if(androidSplash){
                        splashViewModel.hideAndroidSplash()
                    }

                    navController.navigateToScreen(
                        R.id.blockAppFragment,
                    )
                }

                AppState.ErrorLoading -> {
                    splashViewModel.setErrorUiState()
                    if(androidSplash){
                        splashViewModel.hideAndroidSplash()
                    }
                }

                AppState.Loading -> {
                    if (navController.currentDestination?.id != R.id.splashFragment) {
                        navController.navigateToScreen(R.id.splashFragment)
                    }
                }

                AppState.UserError -> {
                    splashViewModel.logout().join()
                    activityViewModel.setAppState()
                }
            }
        }


    private fun NavController.navigateToScreen(@IdRes screenId: Int) = navigate(
        screenId,
        null,
        navOptions { launchSingleTop = true }
    )

    private fun fetchDataForScreens() = lifecycleScope.launch {
        splashViewModel.sendFirebaseToken()
        favoriteViewModel.fetchFavoriteProducts()
        homeViewModel.fetchHomeDetails()
        catalogViewModel.fetchCatalogDetails()
        cartFlowViewModel.fetchCartDetails()
        profileViewModel.fetchProfileDetails()
        delay(400)
    }


    private fun handlePushData() = lifecycleScope.launch {
        debugLog { "splash args $arguments" }

        arguments
            ?.getString("push")
            ?.also { debugLog { "splash push extra: $it" } }
            ?.takeIf { arg -> arg.isNotBlank() }
            ?.let { extra ->
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    siteStateManager.savePushData(JSONObject(extra))
                }
            }
    }
}