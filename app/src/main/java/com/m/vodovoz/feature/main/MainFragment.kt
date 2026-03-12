package com.m.vodovoz.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.google.android.material.snackbar.Snackbar
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.update.AppUpdateController
import com.m.vodovoz.core.android.locationPermissionGranted
import com.m.vodovoz.core.android.locationPermissions
import com.m.vodovoz.core.android.notificationPermissionGranted
import com.m.vodovoz.core.navigation.setupWithNavController
import com.m.vodovoz.databinding.FragmentMainBinding
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.insets.InsetsPadding
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.insets.consumeWindowInsets
import com.m.vodovoz.ui.insets.ime.handleImeInsetIfNeeded
import com.m.vodovoz.ui.insets.ime.removeImeHandling
import com.m.vodovoz.ui.insets.plus
import com.m.vodovoz.ui.insets.toInsetsPadding
import com.m.vodovoz.ui.insets.updatePadding
import com.m.vodovoz.ui.snackbar.SnackbarHostStateOwner
import com.m.vodovoz.util.extensions.doWhenAttached
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment(), SnackbarHostStateOwner {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    private val homeFlowViewModel by activityViewModels<HomeFlowViewModel>()
    private val profileFlowViewModel by activityViewModels<ProfileFlowViewModel>()
    private val catalogFlowViewModel by activityViewModels<CatalogFlowViewModel>()
    private val favoriteFlowViewModel by activityViewModels<FavoriteFlowViewModel>()

    private val cartFlowViewModel by activityViewModels<CartFlowViewModel>()
    private val viewModel: MainViewModel by viewModels()


    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (!requireContext().locationPermissionGranted) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    @Inject
    lateinit var appUpdateFactory: AppUpdateController.Factory
    private val appUpdateController by lazy {
        appUpdateFactory.create { popupSnackbarForCompleteUpdate() }
    }


    override val snackbarHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMainPermissionIfNeeded()
    }

    private fun requestMainPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !requireContext().notificationPermissionGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!requireContext().locationPermissionGranted) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkForUpdate()

        observeCartState()
        observeTabVisibility()

        listenInsetsStates()
        setOnApplyWindowInsets()

    }


    private fun setOnApplyWindowInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(
//            binding.fgvContainer
//        ) { _, applyInsets ->
//            return@setOnApplyWindowInsetsListener WindowInsetsCompat.Builder(
//                applyInsets
//            ).apply {
//                insetsVisibilityState.insets.map { flow ->
//                    flow.value
//                }.forEach { insetState ->
//                    if (insetState.consume) {
//                        consumeWindowInsets(insetState.type)
//                    }
//                }
//            }.build()
//        }
//
//        ViewCompat.setOnApplyWindowInsetsListener(
//            binding.nvNavigation
//        ) { _, _ ->
//            return@setOnApplyWindowInsetsListener CONSUMED
//        }
    }


    private val updateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                accountManager.reportError("Update flow failed! Result code: ${result.resultCode}")
            } else {
                accountManager.reportEvent("Success update!")
            }
        }

    private fun checkForUpdate() {
        appUpdateController.checkForUpdate(updateResultLauncher)
    }


    override fun onStart() {
        super.onStart()
        if (!viewModel.isBottomBarInitialized) {
            setupBottomNavigationBar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                VodovozTheme {
                    CompositionLocalProvider(
                        LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(
                            parent = null
                        )
                    ) {
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                            BottmNav(
                                homeViewModel = homeFlowViewModel,
                                catalogFlowViewModel = catalogFlowViewModel,
                                favoriteFlowViewModel = favoriteFlowViewModel,
                                profileFlowViewModel = profileFlowViewModel,
                                cartFlowViewModel = cartFlowViewModel
                            )
                            VodovozSnackbarHost(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 32.dp),
                                hostState = snackbarHostState,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }


                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.isBottomBarInitialized = false
    }

    private fun observeTabVisibility() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            tabManager.observeTabVisibility().collect { isVisible ->
//                val bottomNavigationView = binding.nvNavigation
//                if (isVisible) {
//                    bottomNavigationView.apply {
//                        animate().cancel()
//                        alpha = if (isVisible) 1f else 0f
//                        visibility = View.VISIBLE
//                        animate().alpha(1f).setInterpolator(
//                            LinearInterpolator()
//                        ).setDuration(300).start()
//                    }
//                } else {
//                    bottomNavigationView.apply { visibility = View.GONE }
//                }
            }
        }
    }

    private fun listenInsetsStates() = combine(
        insetsVisibilityState.insets
    ) { insetsStates -> insetsStates.toList() }.flowWithLifecycle(lifecycle)
        .onEach { insetsStates ->
//            binding.root.doWhenAttached {
//                var accInsetsPadding = InsetsPadding(0, 0, 0, 0)
//
//                for (insetState in insetsStates) {
//                    if (insetState.type == Type.ime()) {
//                        if (insetState.consume) binding.root.handleImeInsetIfNeeded()
//                        else binding.root.removeImeHandling()
//                        continue
//                    }
//
//                    val insets = ViewCompat.getRootWindowInsets(binding.root)
//                    insets?.getInsetsIgnoringVisibility(insetState.type)?.toInsetsPadding()
//                        ?.takeIf { insetState.consume }
//                        ?.let { insetsPadding ->
//                            accInsetsPadding += insetsPadding
//                        }
//                }
//
//                binding.root.updatePadding(accInsetsPadding)
//                binding.root.requestApplyInsets()
//            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)


    @SuppressLint("UseKtx", "StringFormatMatches")
    private fun observeCartState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                tabManager.observeBottomNavCartState().collect { state ->
//                    if (state == null || state.count == 0) {
//                        binding.circleAmount.isVisible = false
//                        binding.nvNavigation.menu.getItem(2).title = getString(R.string.cart)
//                    } else {
//                        binding.circleAmount.text = state.count.toString()
//                        binding.circleAmount.isVisible = true
//                        binding.circleAmount
//                            .animate()
//                            .scaleX(1.4f)
//                            .scaleY(1.4f)
//                            .setDuration(300)
//                            .setInterpolator(AccelerateInterpolator())
//                            .withEndAction {
//                                binding.circleAmount.animate()
//                                    .scaleX(1f)
//                                    .scaleY(1f)
//                            }
//                            .start()
//                        binding.nvNavigation.menu.getItem(2).title =
//                            getString(R.string.price_text, state.total)
//                    }
//                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        appUpdateController.onResumeAction()
    }


    private fun setupBottomNavigationBar() = lifecycleScope.launch {
        viewModel.isBottomBarInitialized = true
    }


    private fun popupSnackbarForCompleteUpdate() {
        val snackbar = Snackbar.make(
            requireView(),
            getString(R.string.update_is_downloaded),
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction(getString(R.string.update)) { _ ->
            appUpdateController.completeUpdate()
        }
        snackbar.setDuration(5000)
        snackbar.setActionTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.bluePrimary
            )
        )
        snackbar.show()
    }

}
