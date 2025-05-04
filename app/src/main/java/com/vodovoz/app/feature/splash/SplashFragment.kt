package com.vodovoz.app.feature.splash

import android.animation.Animator
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.account.data.ReloginManager
import com.vodovoz.app.common.content.BaseFragment
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.databinding.FragmentSplashBinding
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.catalog.CatalogFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.base.MainActivityViewModel
import com.vodovoz.app.ui.base.SplashFileViewModel
import com.vodovoz.app.ui.base.model.AppState
import com.vodovoz.app.ui.extensions.ContextExtensions.isTablet
import com.vodovoz.app.util.SplashFileConfig
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.disableFullScreen
import com.vodovoz.app.util.extensions.enableFullScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    override fun layout(): Int = R.layout.fragment_splash

    private val binding: FragmentSplashBinding by viewBinding {
        FragmentSplashBinding.bind(contentView)
    }

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
    lateinit var reloginManager: ReloginManager

    override fun onStart() {
        super.onStart()
        requireActivity().enableFullScreen()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().disableFullScreen()
    }


    @OptIn(FlowPreview::class)
    private fun listenAppState() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            activityViewModel.appState.debounce(30L).collect { appState ->

                val navController = findNavController()

                when (appState) {
                    AppState.App -> {
                        if (splashViewModel.isLoading.value) {
                            fetchDataForScreens().join()
                        }
                        navController.navigateToScreen(
                            R.id.mainFragment,
                        )
                    }

                    AppState.Blocked -> {
                        navController.navigateToScreen(
                            R.id.blockAppFragment,
                        )
                    }

                    AppState.ErrorLoading -> {
                        showError(ErrorState.NetworkError())
                    }

                    AppState.Loading -> {
                        if (navController.currentDestination?.id != R.id.splashFragment) {
                            navController.navigateToScreen(R.id.splashFragment)
                        }
                    }
                }
            }

        }
    }

    private fun NavController.navigateToScreen(@IdRes screenId: Int){
        navigate(
            screenId,
            null,
            navOptions { launchSingleTop = true }
        )

    }


    private fun refreshApp() = lifecycleScope.launch {
        activityViewModel.checkAppState()
        fetchDataForScreens()
    }

    private fun fetchDataForScreens() = lifecycleScope.launch {
        splashViewModel.sendFirebaseToken()

        favoriteViewModel.fetchFavoriteProducts()
        homeViewModel.fetchHomeDetails()
        catalogViewModel.fetchCatalogDetails()
        cartFlowViewModel.fetchCartDetails()
        profileViewModel.fetchProfileDetails()
        delay(200)
        splashViewModel.finishLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenAppState()
        accountManager.reportEvent("Зашел в приложение")

        bindErrorRefresh {
            refreshApp()
        }

        val lottieSplashView = binding.lottieSplashView

        lottieSplashView.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animation: Animator) {
                kotlin.runCatching {
                    val fileViewModel: SplashFileViewModel by activityViewModels()
                    fileViewModel.finishFileLoading()
                }
            }

            override fun onAnimationEnd(animation: Animator) = Unit

            override fun onAnimationCancel(animation: Animator) = Unit

            override fun onAnimationRepeat(animation: Animator) = Unit
        }
        )

        if (requireContext().isTablet()) {
            lottieSplashView.cancelAnimation()
            lottieSplashView.visibility = View.GONE
            binding.logoLayout.visibility = View.VISIBLE
        } else {
            lottieSplashView.setOutlineMasksAndMattes(true)
            lottieSplashView.enableMergePathsForKitKatAndAbove(true)
            val file = SplashFileConfig.getSplashFile(requireContext())
            if (!file.exists()) {
                debugLog { "file is not exist" }
                lottieSplashView.setFailureListener {
                    debugLog { it.message.toString() }
                    lottieSplashView.clearAnimation()
                }
                lottieSplashView.setAnimationFromUrl(SplashFileConfig.DAFAULT_LINK)
                lottieSplashView.playAnimation()
            } else {
                initAnimation()
            }
        }



        handlePushData()
    }

    private fun initAnimation() {
        val localFile = SplashFileConfig.getSplashFile(requireContext())
        with(binding) {
            if (localFile.exists()) {
                try {
                    val inputStream: InputStream = FileInputStream(localFile)
                    lottieSplashView.setFailureListener {
                        debugLog { it.message.toString() }
                        logoLayout.visibility = View.VISIBLE
                        lottieSplashView.visibility = View.GONE
                        lottieSplashView.clearAnimation()
                    }
                    lottieSplashView.setAnimation(inputStream, null)
                    lottieSplashView.playAnimation()
                } catch (e: Exception) {
                    debugLog { e.message.toString() }
                }
            } else {
                debugLog { "file is not exist" }
                lottieSplashView.visibility = View.GONE
                logoLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun handlePushData() {
        debugLog { "splash args $arguments" }
        if (arguments != null) {
            debugLog { "splash containsKey ${requireArguments().containsKey("push")}" }
            if (requireArguments().containsKey("push")) {
                val extra = requireArguments().getString("push")
                debugLog { "splash get push extra $extra" }
                if (!extra.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.CREATED) {
                            siteStateManager.savePushData(JSONObject(extra))
                        }
                    }
                }
            }
        }
    }
}