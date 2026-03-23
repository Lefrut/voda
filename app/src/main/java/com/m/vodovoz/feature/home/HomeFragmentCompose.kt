package com.m.vodovoz.feature.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.Keep
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.ContentSearchNavigator
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
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToServiceDetails
import com.m.vodovoz.core.navigation.navigateToStories
import com.m.vodovoz.core.navigation.navigateToViewedProductList
import com.m.vodovoz.core.navigation.navigateToWaterApp
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.debugLog
import com.m.vodovoz.util.extensions.isVpnActive
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault
import javax.inject.Inject
import androidx.core.net.toUri
import com.m.vodovoz.common.model.VodovozAction

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var siteStateManager: SiteStateManager

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var navigatorFactory: ContentSearchNavigator.Factory

    private lateinit var searchNavigator: ContentSearchNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchNavigator = navigatorFactory.create(
            findNavController(), this
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeTabReselect()
        observePushFromSiteState()
        observeDeepLinkFromSiteState()
    }

    override fun onResume() {
        super.onResume()
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.remove<Long>("ratedProductId")
            ?.let { productId ->
                viewModel.removeUnratedProduct(productId)
            }

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
                    val viewState by viewModel.collectAsState()
                    val snackbarHostState = remember { SnackbarHostState() }

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
                            mainCoroutineScope = this,
                            snackbarHostState = snackbarHostState,
                            context = context
                        )
                    }

                    LifecycleEffect {
                        viewModel.listenStories()
                    }

                }
            }
        }
    }

    private suspend fun listenEvents(
        mainCoroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        context: Context,
    ): Unit =
        viewModel.events.onSubscription {
            val showedVpnWarning = viewModel.state.value.showedVpnWarning
            if (context.isVpnActive() && !showedVpnWarning) {
                viewModel.showVpnWaring()
            }
        }.collect { event ->
            when (event) {
                is HomeFlowViewModel.HomeEvents.GoToPreOrder -> {
                    findNavController().navigateToPreOrder(event.id)
                }

                is HomeFlowViewModel.HomeEvents.GoToProfile -> {
                    tabManager.apply {
                        setAuthRedirect(findNavController().graph.id)
                        selectTab(R.id.graph_profile)
                    }
                }

                is HomeFlowViewModel.HomeEvents.GoToStories -> {
                    findNavController().navigateToStories(event.storyId, event.stories)
                }

                is HomeFlowViewModel.HomeEvents.GoToProductDetails -> {
                    findNavController().navigateToProductDetails(event.productId)
                }

                is HomeFlowViewModel.HomeEvents.GoToPromotionDetails -> {
                    findNavController().navigateToPromotionDetails(event.promotionId)
                }


                HomeFlowViewModel.HomeEvents.GoToSearch -> {
                    findNavController().navigateToSearch()
                }

                is HomeFlowViewModel.HomeEvents.GoToCategoryProductList -> {
                    findNavController().navigateToCategoryProductList(event.categoryId)
                }

                HomeFlowViewModel.HomeEvents.ShowSpeechRecognizer -> {
                    searchNavigator.navigateToVoiceSearch()
                }

                HomeFlowViewModel.HomeEvents.GoToOrdersHistory -> {
                    findNavController().navigateToOrdersHistory()
                }

                is HomeFlowViewModel.HomeEvents.GoToOrderDetails -> {
                    findNavController().navigateToOrderDetails(event.orderId)
                }

                is HomeFlowViewModel.HomeEvents.GoToWebView -> {
                    findNavController().navigateToWebView(
                        event.url,
                        event.title.ifEmpty { requireContext().getString(R.string.space) }
                    )
                }

                is HomeFlowViewModel.HomeEvents.GoToProductAnalogs -> {
                    findNavController().navigateToProductAnalogs(event.productId)
                }

                HomeFlowViewModel.HomeEvents.GoToQrCode -> {
                    searchNavigator.navigateToImageSearch()
                }

                is HomeFlowViewModel.HomeEvents.ShowSnackbar -> {
                    mainCoroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(event.message)
                    }
                }

                is HomeFlowViewModel.HomeEvents.WriteComment -> {
                    findNavController().navigateToWriteComment(
                        event.productId,
                        event.productName,
                        event.productImage,
                        event.rating
                    )
                }

                HomeFlowViewModel.HomeEvents.CloseApp -> {
                    requireActivity().finish()
                }

                HomeFlowViewModel.HomeEvents.GoToViewedProductList -> {
                    findNavController().navigateToViewedProductList()
                }

                is HomeFlowViewModel.HomeEvents.OpenGooglePlay -> {
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "market://details?id=${context.packageName}".toUri()
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, event.url.toUri()))
                    } finally {
                    }

                }

                is HomeFlowViewModel.HomeEvents.ActivateAction -> {
                    event.action.activate(
                        navController = findNavController(),
                        context = requireActivity(),
                        cookie = cookieManager.fetchCookieSessionId() ?: "",
                        tabManager = tabManager
                    ) { action ->
                        if (action.actionName == VodovozAction.Name.CLOSE) {
                            viewModel.closeSpecialPromotionBottomSheet()
                        }
                    }
                }
            }
        }

    private fun observeTabReselect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager.observeTabReselect()
                    .collect {
                        if (it != TabManager.DEFAULT_STATE && it == R.id.homeFragment) {
                            tabManager.setDefaultState()
                        }
                    }
            }
        }
    }


    @Keep
    private fun observeDeepLinkFromSiteState() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            siteStateManager.observeDeepLinkPath()
                .collect { path ->
                    if (path == null) return@collect
                    if (accountManager.fetchAccountId() == null) {
                        accountManager.setPendingDeeplink(path)
                    }
                    val navController = findNavController()
                    val orderId = path.toLongOrNull()
                    debugLog { "DeepLinkPath: $path" }
                    when {
                        path == "kalkulyator_vody" -> {
                            navController.navigateToWaterApp()
                        }

                        path == AccountManager.ORDERS_DEEPLINK_ID -> {
                            navController.navigateToOrdersHistory()
                        }

                        orderId != null -> {
                            navController.navigateToOrderDetails(orderId)
                        }
                    }

                    siteStateManager.clearDeepLinkListener()
                }
        }
    }


    @Keep
    private fun observePushFromSiteState() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            siteStateManager.observePush().collect { pushData ->
                if (pushData == null) return@collect
                debugLog { "PushFromSiteState: $pushData" }

                val navController = findNavController()
                when (pushData.path) {
                    "AKCII" -> {
                        val promotionId = pushData.id
                        if (promotionId.isNullOrEmpty()) {
                            navController.navigateToPromotions()
                            return@collect
                        }
                        navController.navigateToPromotionDetails(promotionId.toLong())

                    }

                    "TOVAR" -> {
                        val productId = pushData.id
                        if (!productId.isNullOrEmpty()) {
                            navController.navigateToProductDetails(productId.toLong())
                        }
                    }

                    "RAZDEL" -> {
                        val sectionId = pushData.id
                        val blockId = pushData.blockId

                        if (sectionId.isNullOrEmpty()) return@collect


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
                        if (section == getString(R.string.about_store)) {
                            navController.navigateToWebView(
                                VodovozWebConfig.ABOUT_SHOP_URL,
                                getString(R.string.about_store)
                            )
                        }
                        if (section == getString(R.string.contact_us)) {
                            tabManager.apply {
                                setAuthRedirect(navController.graph.id)
                                selectTab(R.id.graph_profile)
                            }
                        }
                    }

                    "dostavka" -> {
                        with(GlobalAppLinks.aboutDelivery) {
                            navController.navigateToWebView(
                                url, title
                            )
                        }
                    }

                    "service" -> {
                        navController.navigateToAllServices()
                    }

                    "remont_kulerov" -> {
                        navController.navigateToServiceDetails(98886)
                    }

                    "feedback" -> {
                        tabManager.apply {
                            setAuthRedirect(navController.graph.id)
                            selectTab(R.id.graph_profile)
                        }
                    }

                    "ACTIONS" -> {
                        navController.navigateToPromotions()
                    }

                    "vseakcii" -> {
                        navController.navigateToPromotions()
                    }

                    "URL" -> {
                        val url = pushData.id ?: return@collect

                        requireContext().openUrl(url)
                    }

                    "trekervodi" -> {
                        navController.navigateToWaterApp()
                    }

                    "profil" -> {
                        tabManager.apply {
                            setAuthRedirect(navController.graph.id)
                            selectTab(R.id.graph_profile)
                        }
                    }

                    "pokypkasertificat" -> {
                        navController.navigateToBuyCertificate()
                    }

                }
                siteStateManager.clearPushListener()
            }
        }
    }
}