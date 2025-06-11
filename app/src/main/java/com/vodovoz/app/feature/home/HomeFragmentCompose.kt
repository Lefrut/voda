package com.vodovoz.app.feature.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.ContentSearchNavigator
import com.vodovoz.app.core.navigation.navigateToAboutApp
import com.vodovoz.app.core.navigation.navigateToAllBrands
import com.vodovoz.app.core.navigation.navigateToAllServices
import com.vodovoz.app.core.navigation.navigateToBrandProductList
import com.vodovoz.app.core.navigation.navigateToBuyCertificate
import com.vodovoz.app.core.navigation.navigateToCategoryProductList
import com.vodovoz.app.core.navigation.navigateToHurryBuyUpProducts
import com.vodovoz.app.core.navigation.navigateToNewProducts
import com.vodovoz.app.core.navigation.navigateToOrderDetails
import com.vodovoz.app.core.navigation.navigateToOrdersHistory
import com.vodovoz.app.core.navigation.navigateToPreOrder
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToPromotionDetails
import com.vodovoz.app.core.navigation.navigateToPromotions
import com.vodovoz.app.core.navigation.navigateToSearch
import com.vodovoz.app.core.navigation.navigateToServiceDetails
import com.vodovoz.app.core.navigation.navigateToStories
import com.vodovoz.app.core.navigation.navigateToWaterApp
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.core.navigation.navigateToWriteComment
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.core.navigation.activate
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.isVpnActive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    lateinit var cookieManager: com.vodovoz.app.common.cookie.CookieManager

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
        observePushFromSiteState()
        observeDeepLinkFromSiteState()
        observeTabReselect()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {

                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)
                    val topProductLazyListState = rememberLazyListState()
                    val pullRefreshState = rememberPullToRefreshState()
                    val snackbarHostState = remember { SnackbarHostState() }

                    when (viewState.uiState) {
                        HomeFlowViewModel.HomeUiState.NetworkError -> {
                            NetworkErrorPlaceholder(
                                onTryAgainClick = { viewModel.refresh() }
                            )
                        }

                        else -> {
                            HomeScreen(
                                viewState = viewState,
                                viewModel = viewModel,
                                pullRefreshState = pullRefreshState,
                                topProductsLazyListState = topProductLazyListState,
                            )
                        }
                    }



                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        VodovozSnackbarHost(hostState = snackbarHostState)
                    }

                    LifecycleEffect(topProductLazyListState, snackbarHostState) {
                        listenEvents(
                            this,
                            topProductLazyListState,
                            snackbarHostState,
                            context
                        )
                    }

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenFavorites(
                            this
                        )
                    }

                    LifecycleEffect {
                        viewModel.listenLoadingProducts()
                    }

                }
            }
        }
    }

    private suspend fun listenEvents(
        mainCoroutineScope: CoroutineScope,
        topProductLazyListState: LazyListState,
        snackbarHostState: SnackbarHostState,
        context: Context,
    ): Unit =
        viewModel.observeEvent().onSubscription {
            val showedVpnWarning = viewModel.observeUiState().value.data.showedVpnWarning
            if (context.isVpnActive() && !showedVpnWarning) {
                viewModel.showVpnWaring()
            }
        }.collect { event ->
            when (event) {
                is HomeFlowViewModel.HomeEvents.GoToPreOrder -> {
                    findNavController().navigateToPreOrder(event.id)
                }

                is HomeFlowViewModel.HomeEvents.GoToProfile -> {
                    tabManager.setAuthRedirect(findNavController().graph.id)
                    tabManager.selectTab(R.id.graph_profile)
                }

                is HomeFlowViewModel.HomeEvents.GoToCart -> {

                }

                is HomeFlowViewModel.HomeEvents.GoToStories -> {
                    findNavController().navigateToStories(event.storyId)
                }

                is HomeFlowViewModel.HomeEvents.GoToProductDetails -> {
                    findNavController().navigateToProductDetails(event.productId)
                }

                is HomeFlowViewModel.HomeEvents.GoToPromotionDetails -> {
                    findNavController().navigateToPromotionDetails(event.promotionId)
                }

                is HomeFlowViewModel.HomeEvents.ActivateButtonAction -> {
                    event.action.activate(
                        navController = findNavController(),
                        tabManager = tabManager
                    )
                }

                HomeFlowViewModel.HomeEvents.GoToSearch -> {
                    findNavController().navigateToSearch()
                }

                HomeFlowViewModel.HomeEvents.ScrollTopProductsToStart -> {
                    topProductLazyListState.animateScrollToItem(0)
                }

                is HomeFlowViewModel.HomeEvents.GoToCategoryProductList -> {
                    findNavController().navigateToCategoryProductList(event.categoryId)
                }

                HomeFlowViewModel.HomeEvents.ShowSpeechRecognizer -> {
                    searchNavigator.navigateToVoiceSearch()
                }

                is HomeFlowViewModel.HomeEvents.ActivateDataAllAction -> {
                    event.action.activate(
                        navController = findNavController(),
                        tabManager = tabManager
                    )
                }

                is HomeFlowViewModel.HomeEvents.ActivateVodovozAction -> {
                    val cookie = cookieManager.fetchCookieSessionId() ?: ""
                    event.action.activate(
                        navController = findNavController(),
                        context = requireActivity(),
                        cookie = cookie,
                        tabManager = tabManager
                    )
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


    private fun observeDeepLinkFromSiteState() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            siteStateManager.observeDeepLinkPath()
                .mapNotNull { path -> path }
                .collect { path ->
                    debugLog { "observeDeepLinkFromSiteState: $path" }

                    when(path) {
                        "catalog" -> {
                            tabManager.selectTab(R.id.graph_catalog)
                        }

                        "action" -> {
                            findNavController().navigateToPromotions()
                        }

                        "brand" -> {
                            findNavController().navigateToAllBrands()
                        }

                        "about" -> {
                            findNavController().navigateToWebView(
                                ApiConfig.ABOUT_SHOP_URL,
                                getString(R.string.about_store)
                            )
                        }

                        "dostavka" -> {
                            findNavController().navigateToWebView(
                                VodovozWebConfig.ABOUT_DELIVERY_URL,
                                getString(R.string.about_delivery)
                            )
                        }

                        "service" -> {
                            findNavController().navigateToAllServices()
                        }

                        "remont_kulerov" -> {
                            findNavController().navigateToServiceDetails(98886)
                        }

                        "feedback" -> {
                            tabManager.selectTab(R.id.graph_profile)
                        }

                        "basket" -> {
                            tabManager.selectTab(R.id.graph_cart)
                        }
                        "mobile_app" -> {
                            findNavController().navigateToAboutApp()
                        }

                        "kalkulyator_vody" -> {
                            accountManager.reportEvent("trekervodi_ssilka")
                            findNavController().navigateToWaterApp()
                        }

                        else -> {
                            val productId = path.removeSuffix("/").takeLastWhile { it.isDigit() }
                                .toLongOrNull() ?: return@collect
                            findNavController().navigateToProductDetails(productId)
                        }

                    }
                    siteStateManager.clearDeepLinkListener()
                }
        }
    }


    private fun observePushFromSiteState() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.RESUMED) {
            siteStateManager.observePush().collect { pushData ->
                debugLog { "push ${pushData?.path} $siteStateManager" }
                when (pushData?.path) {
                    "AKCII" -> {
                        val promotionId = pushData.id
                        if (promotionId.isNullOrEmpty()) return@collect

                        val eventParameters = "\"ID_AKCII\": \"$promotionId\""
                        accountManager.reportEvent("Зашел в акцию (push)", eventParameters)
                        findNavController().navigateToPromotionDetails(promotionId.toLong())

                    }

                    "TOVAR" -> {
                        val productId = pushData.id
                        if (!productId.isNullOrEmpty()) {
                            val eventParameters = "\"ID_Product\": \"$productId\""
                            accountManager.reportEvent(
                                "Зашел в товар (push)",
                                eventParameters
                            )


                            findNavController().navigateToProductDetails(productId.toLong())
                        }
                    }

                    "RAZDEL" -> {
                        val sectionId = pushData.id
                        if (sectionId.isNullOrEmpty()) return@collect

                        val eventParameters = "\"Secition_ID\": \"$sectionId\""
                        accountManager.reportEvent(
                            "Зашел в раздел (push)",
                            eventParameters
                        )

                        findNavController().navigateToCategoryProductList(sectionId.toLong())

                    }

                    "Karta" -> {
                        val orderId = pushData.orderId
                        if (orderId.isNullOrEmpty()) return@collect

                        val eventParameters = "\"ID_Zakaz\": \"$orderId\""
                        accountManager.reportEvent(
                            "Зашел в заказ, статус в пути (push)",
                            eventParameters
                        )

                        findNavController().navigateToOrderDetails(orderId.toLong())

                    }

                    "vsenovinki" -> {
                        findNavController().navigateToNewProducts()
                    }

                    "vseskidki" -> {
                        findNavController().navigateToHurryBuyUpProducts()
                    }

                    "BRAND" -> {
                        val brandId = pushData.id
                        if (!brandId.isNullOrEmpty()) {
                            findNavController().navigateToBrandProductList(brandId.toLong())
                        } else {
                            findNavController().navigateToAllBrands()
                        }
                    }

                    "BRANDY" -> {
                        findNavController().navigateToAllBrands()
                        siteStateManager.clearPushListener()
                    }

                    "about" -> {
                        val section = pushData.section ?: return@collect
                        if (section == getString(R.string.about_store)) {
                            findNavController().navigateToWebView(
                                VodovozWebConfig.ABOUT_SHOP_URL,
                                getString(R.string.about_store)
                            )
                        }
                        if (section == getString(R.string.contact_us)) {
                            viewModel.goToProfile()
                        }
                    }

                    "dostavka" -> {
                        findNavController().navigateToWebView(
                            ApiConfig.ABOUT_DELIVERY_URL,
                            "О доставке"
                        )
                    }

                    "service" -> {
                        findNavController().navigateToAllServices()
                    }

                    "remont_kulerov" -> {
                        findNavController().navigateToAllServices()
                    }

                    "feedback" -> {
                        viewModel.goToProfile()
                    }

                    "TOVARY" -> {
                        findNavController().navigateToCategoryProductList(
                            pushData.id?.toLongOrNull() ?: return@collect
                        )
                    }

                    "ACTIONS" -> {
                        findNavController().navigateToPromotions()
                    }

                    "vseakcii" -> {
                        findNavController().navigateToPromotions()
                    }

                    "URL" -> {
                        val url = pushData.id ?: return@collect

                        findNavController().navigateToWebView(
                            url, requireContext().getString(R.string.space)
                        )
                    }

                    "trekervodi" -> {
                        val eventName = "trekervodi_push"
                        accountManager.reportEvent(eventName)
                        findNavController().navigateToWaterApp()
                    }

                    "profil" -> {
                        viewModel.goToProfile()
                    }

                    "pokypkasertificat" -> {
                        findNavController().navigateToBuyCertificate()
                    }

                    null -> {}
                }
                pushData?.action?.let { action ->
                    if (action.contains("SOBNEW")) {
                        val eventParameters = "\"SOBNEW_NAME\": \"${pushData.id}\""
                        accountManager.reportEvent(
                            "Зашел в приложение (push)",
                            eventParameters
                        )
                    }
                }

                siteStateManager.clearPushListener()
            }
        }
    }
}