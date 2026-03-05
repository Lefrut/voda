package com.m.vodovoz.feature.home

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToAllBrands
import com.m.vodovoz.core.navigation.navigateToAllServices
import com.m.vodovoz.core.navigation.navigateToBannerProductList
import com.m.vodovoz.core.navigation.navigateToBrandProductList
import com.m.vodovoz.core.navigation.navigateToBuyCertificate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToHurryBuyUpProducts
import com.m.vodovoz.core.navigation.navigateToNewProducts
import com.m.vodovoz.core.navigation.navigateToOrderDetails
import com.m.vodovoz.core.navigation.navigateToOrdersHistory
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToPromotionDetails
import com.m.vodovoz.core.navigation.navigateToPromotions
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToServiceDetails
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.core.navigation.navigateToStories
import com.m.vodovoz.core.navigation.navigateToViewedProductList
import com.m.vodovoz.core.navigation.navigateToWaterApp
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.debugLog
import com.m.vodovoz.util.extensions.isVpnActive
import com.m.vodovoz.util.extensions.openUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault

@Composable
fun HomeEntry(
    viewModel: HomeFlowViewModel,
) {
    val viewState by viewModel.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val navController: NavController = LocalView.current.findNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) navController.navigateToQrCode()
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) navController.navigateToSpeechDialog()
    }

    LifecycleEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.remove<Long>("ratedProductId")
            ?.let { productId ->
                viewModel.removeUnratedProduct(productId)
            }
    }

    BackHandler {
        viewModel.showExitDialog()
    }

    HomeScreen(
        viewState = viewState,
        viewModel = viewModel,
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        VodovozSnackbarHost(hostState = snackbarHostState)
    }

    LifecycleEffect(snackbarHostState) {
        listenEvents(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState,
            mainCoroutineScope = this,
            context = context,
            activity = activity,
            cameraPermissionLauncher = cameraPermissionLauncher,
            audioPermissionLauncher = audioPermissionLauncher
        )
    }

    LifecycleEffect {
        viewModel.listenStories()
    }

    LifecycleEffect {
        viewModel.tabManager.observeTabReselect().collect {
            if (it != TabManager.DEFAULT_STATE && it == R.id.homeFragment) {
                viewModel.tabManager.setDefaultState()
            }
        }
    }

    LifecycleEffect {
        observeDeepLinkFromSiteState(viewModel, navController, context)
    }

    LifecycleEffect {
        observePushFromSiteState(viewModel, navController, context)
    }
}

private suspend fun listenEvents(
    viewModel: HomeFlowViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    mainCoroutineScope: CoroutineScope,
    context: android.content.Context,
    activity: Activity?,
    cameraPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    audioPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
): Unit =
    viewModel.events.onSubscription {
        val showedVpnWarning = viewModel.state.value.showedVpnWarning
        if (context.isVpnActive() && !showedVpnWarning) {
            viewModel.showVpnWaring()
        }
    }.collect { event ->
        when (event) {
            is HomeFlowViewModel.HomeEvents.GoToPreOrder -> {
                navController.navigateToPreOrder(event.id)
            }

            is HomeFlowViewModel.HomeEvents.GoToProfile -> {
                viewModel.tabManager.apply {
                    setAuthRedirect(navController.graph.id)
                    selectTab(R.id.graph_profile)
                }
            }

            is HomeFlowViewModel.HomeEvents.GoToStories -> {
                navController.navigateToStories(event.storyId, event.stories)
            }

            is HomeFlowViewModel.HomeEvents.GoToProductDetails -> {
                navController.navigateToProductDetails(event.productId)
            }

            is HomeFlowViewModel.HomeEvents.GoToPromotionDetails -> {
                navController.navigateToPromotionDetails(event.promotionId)
            }

            HomeFlowViewModel.HomeEvents.GoToSearch -> {
                navController.navigateToSearch()
            }

            is HomeFlowViewModel.HomeEvents.GoToCategoryProductList -> {
                navController.navigateToCategoryProductList(event.categoryId)
            }

            HomeFlowViewModel.HomeEvents.ShowSpeechRecognizer -> {
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

            HomeFlowViewModel.HomeEvents.GoToOrdersHistory -> {
                navController.navigateToOrdersHistory()
            }

            is HomeFlowViewModel.HomeEvents.GoToOrderDetails -> {
                navController.navigateToOrderDetails(event.orderId)
            }

            is HomeFlowViewModel.HomeEvents.GoToWebView -> {
                navController.navigateToWebView(
                    event.url,
                    event.title.ifEmpty { context.getString(R.string.space) }
                )
            }

            is HomeFlowViewModel.HomeEvents.GoToProductAnalogs -> {
                navController.navigateToProductAnalogs(event.productId)
            }

            HomeFlowViewModel.HomeEvents.GoToQrCode -> {
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

            is HomeFlowViewModel.HomeEvents.ShowSnackbar -> {
                mainCoroutineScope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            is HomeFlowViewModel.HomeEvents.WriteComment -> {
                navController.navigateToWriteComment(
                    event.productId,
                    event.productName,
                    event.productImage,
                    event.rating
                )
            }

            HomeFlowViewModel.HomeEvents.CloseApp -> {
                activity?.finish()
            }

            HomeFlowViewModel.HomeEvents.GoToViewedProductList -> {
                navController.navigateToViewedProductList()
            }

            is HomeFlowViewModel.HomeEvents.OpenGooglePlay -> {
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "market://details?id=${context.packageName}".toUri()
                        )
                    )
                } catch (_: ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, event.url.toUri()))
                }
            }

            is HomeFlowViewModel.HomeEvents.ActivateAction -> {
                event.action.activate(
                    navController = navController,
                    context = context,
                    cookie = viewModel.cookieManager.fetchCookieSessionId() ?: "",
                    tabManager = viewModel.tabManager
                ) { action ->
                    if (action.actionName == VodovozAction.Name.CLOSE) {
                        viewModel.closeSpecialPromotionBottomSheet()
                    }
                }
            }
        }
    }

@Keep
private suspend fun observeDeepLinkFromSiteState(
    viewModel: HomeFlowViewModel,
    navController: NavController,
    context: android.content.Context,
) {
    viewModel.siteStateManager.observeDeepLinkPath().collect { path ->
        if (path == null) return@collect

        if (viewModel.accountManager.fetchAccountId() == null) {
            viewModel.accountManager.setPendingDeeplink(path)
        }

        val orderId = path.toLongOrNull()
        debugLog { "DeepLinkPath: $path" }
        when {
            path == "kalkulyator_vody" -> {
                viewModel.accountManager.reportEvent("trekervodi_ssilka")
                navController.navigateToWaterApp()
            }

            path == AccountManager.ORDERS_DEEPLINK_ID -> {
                navController.navigateToOrdersHistory()
            }

            orderId != null -> {
                navController.navigateToOrderDetails(orderId)
            }
        }

        viewModel.siteStateManager.clearDeepLinkListener()
    }
}

@Keep
private suspend fun observePushFromSiteState(
    viewModel: HomeFlowViewModel,
    navController: NavController,
    context: android.content.Context,
) {
    viewModel.siteStateManager.observePush().collect { pushData ->
        if (pushData == null) return@collect
        debugLog { "PushFromSiteState: $pushData" }

        when (pushData.path) {
            "AKCII" -> {
                val promotionId = pushData.id
                if (promotionId.isNullOrEmpty()) {
                    navController.navigateToPromotions()
                    return@collect
                }

                val eventParameters = "\"ID_AKCII\": \"$promotionId\""
                viewModel.accountManager.reportEvent("Зашел в акцию (push)", eventParameters)
                navController.navigateToPromotionDetails(promotionId.toLong())
            }

            "TOVAR" -> {
                val productId = pushData.id
                if (!productId.isNullOrEmpty()) {
                    val eventParameters = "\"ID_Product\": \"$productId\""
                    viewModel.accountManager.reportEvent("Зашел в товар (push)", eventParameters)
                    navController.navigateToProductDetails(productId.toLong())
                }
            }

            "RAZDEL" -> {
                val sectionId = pushData.id
                val blockId = pushData.blockId

                if (sectionId.isNullOrEmpty()) return@collect

                val eventParameters = "\"Secition_ID\": \"$sectionId\""
                viewModel.accountManager.reportEvent("Зашел в раздел (push)", eventParameters)

                if (!blockId.isNullOrEmpty()) {
                    navController.navigateToBannerProductList(
                        bannerId = sectionId.toLongOrDefault(-1),
                        blockId = blockId.toLongOrDefault(-1)
                    )
                } else {
                    navController.navigateToCategoryProductList(
                        categoryId = sectionId.toLong()
                    )
                }
            }

            "TOVARY" -> {
                val sectionId = pushData.id
                val blockId = pushData.blockId

                if (!sectionId.isNullOrBlank() && !blockId.isNullOrBlank()) {
                    navController.navigateToBannerProductList(
                        bannerId = sectionId.toLongOrDefault(-1),
                        blockId = blockId.toLongOrDefault(-1)
                    )
                }
            }

            "Karta" -> {
                val orderId = pushData.orderId
                if (orderId.isNullOrEmpty()) return@collect

                val eventParameters = "\"ID_Zakaz\": \"$orderId\""
                viewModel.accountManager.reportEvent(
                    "Зашел в заказ, статус в пути (push)",
                    eventParameters
                )

                navController.navigateToOrderDetails(orderId.toLong())
            }

            "vsenovinki" -> {
                navController.navigateToNewProducts()
            }

            "vseskidki" -> {
                navController.navigateToHurryBuyUpProducts()
            }

            "BRAND" -> {
                val brandId = pushData.id
                if (!brandId.isNullOrEmpty()) {
                    navController.navigateToBrandProductList(brandId.toLong())
                } else {
                    navController.navigateToAllBrands()
                }
            }

            "BRANDY" -> {
                navController.navigateToAllBrands()
            }

            "about" -> {
                val section = pushData.section ?: return@collect
                if (section == context.getString(R.string.about_store)) {
                    navController.navigateToWebView(
                        VodovozWebConfig.ABOUT_SHOP_URL,
                        context.getString(R.string.about_store)
                    )
                }
                if (section == context.getString(R.string.contact_us)) {
                    viewModel.tabManager.apply {
                        setAuthRedirect(navController.graph.id)
                        selectTab(R.id.graph_profile)
                    }
                }
            }

            "dostavka" -> {
                with(GlobalAppLinks.aboutDelivery) {
                    navController.navigateToWebView(url, title)
                }
            }

            "service" -> {
                navController.navigateToAllServices()
            }

            "remont_kulerov" -> {
                navController.navigateToServiceDetails(98886)
            }

            "feedback" -> {
                viewModel.tabManager.apply {
                    setAuthRedirect(navController.graph.id)
                    selectTab(R.id.graph_profile)
                }
            }

            "ACTIONS", "vseakcii" -> {
                navController.navigateToPromotions()
            }

            "URL" -> {
                val url = pushData.id ?: return@collect
                context.openUrl(url)
            }

            "trekervodi" -> {
                viewModel.accountManager.reportEvent("trekervodi_push")
                navController.navigateToWaterApp()
            }

            "profil" -> {
                viewModel.tabManager.apply {
                    setAuthRedirect(navController.graph.id)
                    selectTab(R.id.graph_profile)
                }
            }

            "pokypkasertificat" -> {
                navController.navigateToBuyCertificate()
            }
        }

        pushData.action?.let { action ->
            if (action.contains("SOBNEW")) {
                val eventParameters = "\"SOBNEW_NAME\": \"${pushData.id}\""
                viewModel.accountManager.reportEvent(
                    "Зашел в приложение (push)",
                    eventParameters
                )
            }
        }

        viewModel.siteStateManager.clearPushListener()
    }
}
