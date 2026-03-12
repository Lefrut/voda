package com.m.vodovoz.feature.catalog

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.core.navigation.navigateToSubCategories
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState

@Composable
fun CatalogEntry(
    viewModel: CatalogFlowViewModel,
) {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            navigator.navigateToQrCode()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            navigator.navigateToSpeechDialog()
        }
    }

    when (viewState.uiState) {
        CatalogFlowViewModel.CatalogUiState.Error -> {
            NetworkErrorPlaceholder { viewModel.fetchCatalogDetails() }
        }

        else -> {
            CatalogScreen(viewModel = viewModel, viewState = viewState)
        }
    }

    LifecycleEffect {
        viewModel.tabManager.observeTabReselect().collect {
            if (it != com.m.vodovoz.common.tab.TabManager.DEFAULT_STATE && it == R.id.catalogFragment) {
                viewModel.tabManager.setDefaultState()
            }
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                is CatalogFlowViewModel.CatalogEvents.GoToProfile -> {
                    viewModel.tabManager.setAuthRedirect(navigator.graph.id)
                    viewModel.tabManager.selectTab(R.id.graph_profile)
                }

                CatalogFlowViewModel.CatalogEvents.GoToSearch -> {
                    navigator.navigateToSearch()
                }

                is CatalogFlowViewModel.CatalogEvents.GoToSubCategories -> {
                    navigator.navigateToSubCategories(event.catalogCategory)
                }

                is CatalogFlowViewModel.CatalogEvents.GoToProductList -> {
                    navigator.navigateToCategoryProductList(event.catalogCategory.id)
                }

                CatalogFlowViewModel.CatalogEvents.GoToScanner -> {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        navigator.navigateToQrCode()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                CatalogFlowViewModel.CatalogEvents.ShowSpeechRecognizer -> {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        navigator.navigateToSpeechDialog()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                is CatalogFlowViewModel.CatalogEvents.ActivateAction -> {
                    event.action.activate(
                        navigator = navigator,
                        context = context,
                        cookie = viewModel.cookieManager.fetchCookieSessionId() ?: "",
                        tabManager = viewModel.tabManager
                    )
                }
            }
        }
    }
}
