package com.m.vodovoz.feature.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.feature.splash.model.SplashEvent
import com.m.vodovoz.ui.base.MainActivityViewModel
import com.m.vodovoz.ui.base.model.AppState
import com.m.vodovoz.ui.base.model.SplashFileState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.VodovozSplashFile
import com.m.vodovoz.util.extensions.disableFullScreen
import com.m.vodovoz.util.extensions.enableFullScreen
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : Fragment() {


    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val splashViewModel: SplashViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val catalogViewModel: CatalogFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()


    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var siteStateManager: SiteStateManager

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        requireActivity().enableFullScreen()
    }

    override fun onStop() {
        activityViewModel.finishAndroidSplash()
        requireActivity().disableFullScreen()
        super.onStop()
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

                    LaunchedEffect(Unit) {
                        activityViewModel.finishAndroidSplash()
                    }

                    val viewState by splashViewModel.collectAsState()
                    val context = LocalContext.current

                    AppSplashScreen(
                        viewModel = splashViewModel,
                        viewState = viewState
                    )

                    LifecycleEffect {
                        activityViewModel.fileState.collectLatest { fileState ->
                            when (fileState) {
                                SplashFileState.Error -> {
                                    splashViewModel.hideAndroidSplash()
                                }

                                SplashFileState.Success -> {
                                    splashViewModel.changeToAnimation(
                                        VodovozSplashFile.getSplashFile(context)
                                    )
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
    }

    private suspend fun listenEvents(): Unit = splashViewModel.events.collect { event ->
        when (event) {
            SplashEvent.RefreshApp -> {
                activityViewModel.fetchAppConfig()
            }

            SplashEvent.HideAndroidSplash -> {
                activityViewModel.finishAndroidSplash()
            }
        }
    }

    private suspend fun listenAppState(): Unit =
        activityViewModel.appState.collect { appState ->

            val navController = findNavController()
            val androidSplash = activityViewModel.androidSplash.value

            when (appState) {
                AppState.App -> {
                    fetchDataForScreens().join()
                    MapKitFactory.setApiKey(
                        siteStateManager.siteStateSnapshot.mapkitKey
                    )
                    MapKitFactory.initialize(requireActivity())
                    navController.navigateToScreen(R.id.mainFragment)
                }

                AppState.Blocked -> {
                    if (androidSplash) {
                        splashViewModel.hideAndroidSplash()
                    }

                    navController.navigateToScreen(
                        R.id.blockAppFragment,
                    )
                }

                AppState.ErrorLoading -> {
                    delay(100L)
                    splashViewModel.setErrorUiState()
                    if (androidSplash) {
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
        resId = screenId,
        args = null,
        navOptions = navOptions {
            currentDestination?.id?.let { id ->
                popUpTo(id) { inclusive = true }
            }
            launchSingleTop = true
        }
    )

    private fun fetchDataForScreens() = lifecycleScope.launch {
        splashViewModel.sendFirebaseToken()
        val syncFavoritesJob = splashViewModel.syncFavorites()

        val importantJob = launch {
            homeViewModel.fetchHomeDetails {
                if (it != 0) return@fetchHomeDetails
                syncFavoritesJob.join()
                favoriteViewModel.fetchFavoriteProducts()
                cancel()
            }.join()
        }
        importantJob.join()
        val jobs = listOf(
            catalogViewModel.fetchCatalogDetails(),
            profileViewModel.fetchProfileDetails(),
            cartFlowViewModel.fetchCartDetails()
        )
        tabManager.updateBottomNavCartState()
        jobs.joinAll()
    }
}