package com.vodovoz.app.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.common.update.AppUpdateController
import com.vodovoz.app.core.android.locationPermissionGranted
import com.vodovoz.app.core.android.locationPermissions
import com.vodovoz.app.core.android.notificationPermissionGranted
import com.vodovoz.app.core.navigation.setupWithNavController
import com.vodovoz.app.databinding.FragmentMainBinding
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import com.vodovoz.app.ui.insets.ime.handleImeInsetIfNeeded
import com.vodovoz.app.ui.insets.ime.removeImeHandling
import com.vodovoz.app.ui.snackbar.SnackbarHostStateOwner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main), SnackbarHostStateOwner {

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}


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

    private val viewModel: MainViewModel by viewModels()

    private val binding: FragmentMainBinding by viewBinding { fragment ->
        FragmentMainBinding.bind(fragment.view ?: View(requireContext()))
    }

    override val snackbarHostState = SnackbarHostState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !requireContext().notificationPermissionGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (!requireContext().locationPermissionGranted) {
            locationPermissionLauncher.launch(locationPermissions)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeTabState()
        observeCartState()
        observeTabVisibility()

        listenImeHandling()
        listenNavigationBarInsets()
        listenStatusBarInsets()

        checkForUpdate()
        setMainOnApplyWindowInsets()

        val snackbarHostView = view.findViewById<ComposeView>(R.id.snackbar_host)
        snackbarHostView.setContent {
            VodovozTheme {
                VodovozSnackbarHost(
                    modifier = Modifier.padding(top = 32.dp),
                    hostState = snackbarHostState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    private fun setMainOnApplyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, applyInsets ->
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.Builder(
                applyInsets
            ).apply {

                if (insetsVisibilityState.navigationBarInsets.value) {
                    setInsets(
                        WindowInsetsCompat.Type.navigationBars(),
                        Insets.NONE
                    )
                }

                if (insetsVisibilityState.statusBarInsets.value) {
                    setInsets(
                        WindowInsetsCompat.Type.statusBars(),
                        Insets.NONE
                    )
                }

                if (insetsVisibilityState.handleIme.value) {
                    setInsets(
                        WindowInsetsCompat.Type.ime(),
                        Insets.NONE
                    )
                }

            }.build()
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.nvNavigation
        ) { _, _ ->
            return@setOnApplyWindowInsetsListener CONSUMED
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.isBottomBarInitialized = false
    }

    private fun observeTabVisibility() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            tabManager.observeTabVisibility().collect { isVisible ->
                val bottomNavigationView = binding.nvNavigation
                if (isVisible) {
                    bottomNavigationView.apply {
                        animate().cancel()
                        alpha = if (visibility == View.VISIBLE) 1f else 0f
                        visibility = View.VISIBLE
                        animate().alpha(1f).setInterpolator(
                            LinearInterpolator()
                        ).setDuration(300).start()
                    }
                } else {
                    bottomNavigationView.apply { visibility = View.GONE }
                }
            }
        }
    }

    private fun listenImeHandling() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            insetsVisibilityState.handleIme.collect { handleIme ->
                if (handleIme) {
                    binding.root.handleImeInsetIfNeeded()
                } else {
                    binding.root.removeImeHandling()
                }
            }
        }
    }

    private fun listenNavigationBarInsets() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            insetsVisibilityState.navigationBarInsets.collect { insertInsets ->
                val insets = ViewCompat.getRootWindowInsets(binding.root)
                val bottomPadding = if (insertInsets) insets?.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.navigationBars()
                )?.bottom ?: 0 else 0

                binding.root.requestApplyInsets()
                binding.root.updatePadding(bottom = if (insertInsets) bottomPadding else 0)
            }
        }
    }

    private fun listenStatusBarInsets() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            insetsVisibilityState.statusBarInsets.collect { insertInsets ->
                val insets = ViewCompat.getRootWindowInsets(binding.root)
                val topPadding = if (insertInsets) insets?.getInsetsIgnoringVisibility(
                    WindowInsetsCompat.Type.statusBars()
                )?.top ?: 0 else 0

                binding.root.requestApplyInsets()
                binding.root.updatePadding(top = if (insertInsets) topPadding else 0)
            }
        }

    }


    @SuppressLint("UseKtx")
    private fun observeCartState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager.observeBottomNavCartState().collect { state ->
                    if (state == null || state.count == 0) {
                        binding.circleAmount.isVisible = false
                        binding.nvNavigation.menu.getItem(2).title = getString(R.string.cart)
                    } else {
                        binding.circleAmount.text = state.count.toString()
                        binding.circleAmount.isVisible = true
                        binding.circleAmount
                            .animate()
                            .scaleX(1.4f)
                            .scaleY(1.4f)
                            .setDuration(300)
                            .setInterpolator(AccelerateInterpolator())
                            .withEndAction {
                                binding.circleAmount.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                            }
                            .start()
                        binding.nvNavigation.menu.getItem(2).title =
                            getString(R.string.price_text, state.total)
                    }
                }
            }
        }
    }

    private fun observeTabState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager
                    .observeTabState()
                    .collect { tabId ->
                        binding.nvNavigation.selectedItemId = tabId
                    }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        appUpdateController.onResumeAction()
    }


    private fun setupBottomNavigationBar() = lifecycleScope.launch {
        viewModel.isBottomBarInitialized = true

        val navGraphIds = listOf(
            R.navigation.nav_graph_home,
            R.navigation.nav_graph_catalog,
            R.navigation.nav_graph_cart,
            R.navigation.nav_graph_favorite,
            R.navigation.nav_graph_profile
        )

        val activity = requireActivity()

        val navControllerLiveData = binding.nvNavigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = childFragmentManager,
            containerId = R.id.fgvContainer,
            intent = activity.intent,
            activity = activity,
            lifecycleOwner = viewLifecycleOwner,
            recyclerViewToTop = { menuId ->
                tabManager.reselect(menuId)
            }
        )
        navControllerLiveData.observe(viewLifecycleOwner) { navController ->
            Navigation.setViewNavController(requireView(), navController)
        }
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