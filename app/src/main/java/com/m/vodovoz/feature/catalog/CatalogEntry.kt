package com.m.vodovoz.feature.catalog

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToQrCode
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
    val navController = LocalView.current.findNavController()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            navController.navigateToQrCode()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            navController.navigateToSpeechDialog()
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
                    viewModel.tabManager.setAuthRedirect(navController.graph.id)
                    viewModel.tabManager.selectTab(R.id.graph_profile)
                }

                CatalogFlowViewModel.CatalogEvents.GoToSearch -> {
                    navController.navigate(R.id.searchFragment)
                }

                is CatalogFlowViewModel.CatalogEvents.GoToSubCategories -> {
                    navController.navigateToSubCategories(event.catalogCategory)
                }

                is CatalogFlowViewModel.CatalogEvents.GoToProductList -> {
                    navController.navigateToCategoryProductList(event.catalogCategory.id)
                }

                CatalogFlowViewModel.CatalogEvents.GoToScanner -> {
                    if (
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        navController.navigateToQrCode()
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
                        navController.navigateToSpeechDialog()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                is CatalogFlowViewModel.CatalogEvents.ActivateAction -> {
                    event.action.activate(
                        navController = navController,
                        context = context,
                        cookie = viewModel.cookieManager.fetchCookieSessionId() ?: "",
                        tabManager = viewModel.tabManager
                    )
                }
            }
        }
    }
}
