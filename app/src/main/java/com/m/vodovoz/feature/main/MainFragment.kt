package com.m.vodovoz.feature.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
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
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.snackbar.SnackbarHostStateOwner
import com.m.vodovoz.util.extensions.addOnBackPressedCallback
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val navigationEventOwner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher
                get() = this@MainFragment.requireActivity().navigationEventDispatcher

        }

        return ComposeView(requireContext()).apply {
            setContent {
                VodovozTheme {
                    CompositionLocalProvider(
                        LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(
                            parent = navigationEventOwner
                        )
                    ) {
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                            BottmNav(
                                homeViewModel = homeFlowViewModel,
                                catalogFlowViewModel = catalogFlowViewModel,
                                favoriteFlowViewModel = favoriteFlowViewModel,
                                profileFlowViewModel = profileFlowViewModel,
                                cartFlowViewModel = cartFlowViewModel,
                                tabManager = tabManager
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

    override fun onResume() {
        super.onResume()
        appUpdateController.onResumeAction()
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
