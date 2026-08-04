package com.m.vodovoz.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.CONSUMED
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsCompat.Type.InsetsType
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.NavController
import com.google.android.material.snackbar.Snackbar
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.model.vodovozActionOf
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.update.AppUpdateController
import com.m.vodovoz.core.android.locationPermissionGranted
import com.m.vodovoz.core.android.locationPermissions
import com.m.vodovoz.core.android.notificationPermissionGranted
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.setupWithNavController
import com.m.vodovoz.databinding.FragmentMainBinding
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.ui.insets.InsetsPadding
import com.m.vodovoz.ui.insets.InsetsVisibilityState
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
import kotlin.math.roundToInt


@AndroidEntryPoint
class MainFragment : Fragment(), SnackbarHostStateOwner, FloatingPromoUiHost {

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
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    @Inject
    lateinit var appUpdateFactory: AppUpdateController.Factory
    private val appUpdateController by lazy {
        appUpdateFactory.create { popupSnackbarForCompleteUpdate() }
    }

    private val viewModel: MainViewModel by viewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var selectedNavController: NavController? = null
    private val floatingPromoUiState = FloatingPromoUiStateHolder(
        clickDebounceMillis = FLOATING_PROMO_CLICK_DEBOUNCE_MS,
    )

    private val destinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            floatingPromoUiState.onDestinationChanged(destination.id)
            updateFloatingPromoBottomMargin()
            updateFloatingPromoVisibility()
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

        observeTabState()
        observeCartState()
        observeTabVisibility()
        observeFloatingPromoState()

        listenInsetsStates()
        setOnApplyWindowInsets()
        setupFloatingPromoButton()

        binding.snackbarHost.setContent {
            VodovozTheme {
                VodovozSnackbarHost(
                    modifier = Modifier.padding(top = 32.dp),
                    hostState = snackbarHostState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }

    private fun setOnApplyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            binding.fgvContainer
        ) { _, applyInsets ->
            return@setOnApplyWindowInsetsListener WindowInsetsCompat.Builder(
                applyInsets
            ).apply {
                insetsVisibilityState.insets.map { flow ->
                    flow.value
                }.forEach { insetState ->
                    if (insetState.consume) {
                        consumeWindowInsets(insetState.type)
                    }
                }
            }.build()
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.nvNavigation
        ) { _, _ ->
            return@setOnApplyWindowInsetsListener CONSUMED
        }
    }

    private fun WindowInsetsCompat.Builder.consumeWindowInsets(@InsetsType typeMask: Int) {
        setInsets(typeMask, Insets.NONE)
    }


    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { _: ActivityResult -> }

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
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        selectedNavController?.removeOnDestinationChangedListener(destinationChangedListener)
        selectedNavController = null
        floatingPromoUiState.reset()
        super.onDestroyView()
        viewModel.isBottomBarInitialized = false
        _binding = null
    }

    private fun observeTabVisibility() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            tabManager.observeTabVisibility().collect { isVisible ->
                floatingPromoUiState.setBottomNavigationVisible(isVisible)
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
                updateFloatingPromoVisibility()
            }
        }
    }

    private fun setupFloatingPromoButton() {
        binding.floatingPromoButton.setContent {
            VodovozTheme {
                val state by viewModel.floatingPromoState.collectAsStateWithLifecycle()
                val presentation = floatingPromoUiState.presentation
                state.button?.let { button ->
                    FloatingPromoButton(
                        button = button,
                        isVisible = presentation.isVisible,
                        side = presentation.side,
                        onClick = {
                            handleFloatingPromoClick(
                                action = button.action,
                                id = button.actionId,
                                blockId = button.blockId,
                            )
                        },
                        onCloseClick = ::handleFloatingPromoCloseClick,
                    )
                }
            }
        }
        updateFloatingPromoBottomMargin()
    }

    private fun observeFloatingPromoState() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.floatingPromoState.collect {
                updateFloatingPromoVisibility()
            }
        }
    }

    private fun updateFloatingPromoVisibility() {
        val state = viewModel.floatingPromoState.value
        val side = FloatingPromoScreenRegistry.sideFor(
            destinationId = floatingPromoUiState.destinationId,
            leftScreenNames = state.leftScreenNames,
            rightScreenNames = state.rightScreenNames,
        )
        val isVisible =
            !state.isLoading &&
                state.error == null &&
                state.button != null &&
                floatingPromoUiState.isBottomNavigationVisible &&
                !floatingPromoUiState.isSuppressed &&
                !state.isHiddenByClickCooldown &&
                side != null

        floatingPromoUiState.setPresentation(isVisible = isVisible, side = side)
    }

    private fun updateFloatingPromoBottomMargin() {
        val binding = _binding ?: return
        val density = resources.displayMetrics.density
        val defaultBottomInsetPx = (
            FloatingPromoBannerDefaults.DefaultBottomInset.value * density
            ).roundToInt()
        val productButtonSpacingPx = (
            FloatingPromoBannerDefaults.ProductButtonSpacing.value * density
            ).roundToInt()

        binding.floatingPromoButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = floatingPromoUiState.resolveBottomInset(
                defaultInsetPx = defaultBottomInsetPx,
                productButtonSpacingPx = productButtonSpacingPx,
            )
        }
    }

    private fun handleFloatingPromoClick(action: String, id: String, blockId: Long) {
        val navController = selectedNavController ?: return
        val navigationAction = vodovozActionOf(action, id, blockId) ?: return
        if (navigationAction is VodovozAction.Unknown) return
        if (!floatingPromoUiState.isPromoVisible) return

        val now = SystemClock.elapsedRealtime()
        if (!floatingPromoUiState.tryConsumeClick(now)) return

        viewModel.hideFloatingPromoAfterClick(now)

        runCatching {
            navigationAction.activate(
                navController = navController,
                context = requireActivity(),
                cookie = cookieManager.fetchCookieSessionId().orEmpty(),
                tabManager = tabManager,
            )
        }
    }

    private fun handleFloatingPromoCloseClick() {
        if (!floatingPromoUiState.isPromoVisible) return

        val now = SystemClock.elapsedRealtime()
        if (!floatingPromoUiState.tryConsumeClick(now)) return

        viewModel.hideFloatingPromoAfterClick(now)
    }

    override fun setFloatingPromoExtraBottomOffset(offsetPx: Int?) {
        floatingPromoUiState.setDynamicExtraBottomOffset(offsetPx)
        updateFloatingPromoBottomMargin()
    }

    override fun setFloatingPromoSuppressed(suppressed: Boolean) {
        floatingPromoUiState.setSuppressed(suppressed)
        updateFloatingPromoVisibility()
    }

    private fun listenInsetsStates() = combine(
        insetsVisibilityState.insets
    ) { insetsStates -> insetsStates.toList() }.flowWithLifecycle(lifecycle)
        .onEach { insetsStates ->
            binding.root.doWhenAttached {
                var accInsetsPadding = InsetsPadding(0, 0, 0, 0)

                for (insetState in insetsStates) {
                    if (insetState.type == Type.ime()) {
                        if (insetState.consume) binding.root.handleImeInsetIfNeeded()
                        else binding.root.removeImeHandling()
                        continue
                    }

                    val insets = ViewCompat.getRootWindowInsets(binding.root)
                    insets?.getInsetsIgnoringVisibility(insetState.type)?.toInsetsPadding()
                        ?.takeIf { insetState.consume }
                        ?.let { insetsPadding ->
                            accInsetsPadding += insetsPadding
                        }
                }

                binding.root.updatePadding(accInsetsPadding)
                binding.root.requestApplyInsets()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)


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
        viewModel.refreshFloatingPromoCooldown(SystemClock.elapsedRealtime())
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
            selectedNavController?.removeOnDestinationChangedListener(destinationChangedListener)
            selectedNavController = navController
            navController.addOnDestinationChangedListener(destinationChangedListener)
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

    private companion object {
        const val FLOATING_PROMO_CLICK_DEBOUNCE_MS = 800L
    }

}
