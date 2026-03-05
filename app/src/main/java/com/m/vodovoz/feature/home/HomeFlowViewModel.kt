package com.m.vodovoz.feature.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.model.AppLink
import com.m.vodovoz.common.model.BaseVodovozAction
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.CategoryWithProductsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.PromotionUi
import com.m.vodovoz.design_system.model.SpecialPromotionUi
import com.m.vodovoz.design_system.model.StoryUi
import com.m.vodovoz.design_system.model.VodovozSectionUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.toVodovozSectionUi
import com.m.vodovoz.domain.general.model.product.SuperTopModel
import com.m.vodovoz.domain.general.model.promotion.toUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.home.model.AppUpdateInfoUi
import com.m.vodovoz.feature.home.model.HomeListItem
import com.m.vodovoz.feature.home.model.HomeOrderUi
import com.m.vodovoz.feature.home.model.MenuItemTypeUi
import com.m.vodovoz.feature.home.model.MenuItemUi
import com.m.vodovoz.feature.home.model.PopularCategoryUi
import com.m.vodovoz.feature.home.model.UnratedProductUi
import com.m.vodovoz.feature.home.model.UnratedProductsSectionUi
import com.m.vodovoz.feature.home.model.compareVersions
import com.m.vodovoz.feature.home.model.firstOrNull
import com.m.vodovoz.feature.home.model.firstValueOrNull
import com.m.vodovoz.feature.home.model.plusItem
import com.m.vodovoz.feature.home.model.toUi
import com.m.vodovoz.feature.home.model.withProducts
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.ItemsState
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import com.m.vodovoz.util.extensions.awaitOrNull
import com.m.vodovoz.util.extensions.deferredResult
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class HomeFlowViewModel @Inject constructor(
    val tabManager: TabManager,
    val siteStateManager: SiteStateManager,
    val cookieManager: CookieManager,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<HomeListItem<*>, HomeFlowViewModel.HomeState, HomeFlowViewModel.HomeEvents>(
    state = HomeState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {


    suspend fun listenStories(): Unit = state.map {
        stateSnapshot.items.firstValueOrNull<List<StoryUi>, HomeListItem.Stories>()
            ?: emptyList()
    }.distinctUntilChanged()
        .combine(userPreferencesRepository.viewedStoryIds) { stories, storiesIds ->
            stories.map { story ->
                if (storiesIds.contains(story.id)) story.copy(viewed = true) else story
            }.sortedBy { it.viewed }
        }.shareIn(viewModelScope, SharingStarted.Lazily, 1).collect { updatedStories ->
            updateState { s ->
                s.copy(
                    items = buildList {
                        addAll(s.items)
                        val storyItem = HomeListItem.Stories(updatedStories)
                        removeIf { item -> item is HomeListItem.Stories }
                        add(storyItem)
                    },
                )
            }
        }

    private suspend fun fetchPrimaryDetails(): Boolean {
        val bannersDeferred = vodovozServiceRepository.getBanners().deferredResult()
        val sectionPopularCategoriesDeferred =
            vodovozServiceRepository.getPopularCategories().deferredResult()
        val orderMenuDeferred = vodovozServiceRepository.getOrderMenu().deferredResult()
        val sectionsTopDeferred =
            vodovozServiceRepository.getSuperTopCategories().deferredResult()
        val superTop = sectionsTopDeferred.awaitOrNull() ?: SuperTopModel.Empty

        val sectionTopItem = with(
            HomeListItem.Products.topSection(superTop.topSection.toUi())
        ) {
            val products = vodovozServiceRepository.getSuperTopProducts(
                currentCategoryId
            ).singleResult().getOrNull()?.mapToUi() ?: emptyList()
            withProducts(products)
        }


        val banners = bannersDeferred.awaitOrNull()
        val sectionPopularCategories = sectionPopularCategoriesDeferred.awaitOrNull()
        val orderMenu = orderMenuDeferred.awaitOrNull()

        val bottomSection = superTop.bottomSection.toUi()

        if (banners != null || orderMenu != null || sectionPopularCategories != null) {
            updateState { s ->
                s.copy(
                    uiState = HomeUiState.Success,
                    items = buildList {
                        banners?.let {
                            add(HomeListItem.Banner(banners.mapToUi()))
                        }
                        orderMenu?.let {
                            add(HomeListItem.OrderWithMenu(orderMenu.toUi()))
                        }
                        add(HomeListItem.Divider(350))
                        sectionPopularCategories?.let {
                            add(HomeListItem.PopularCategories(sectionPopularCategories.toUi()))
                        }
                        add(sectionTopItem)
                        add(HomeListItem.Products.bottomSection(bottomSection))
                    }
                )
            }
        } else {
            updateState { s -> s.copy(uiState = HomeUiState.NetworkError) }
            return false
        }
        return true
    }

    private fun <Model, Ui : HomeListItem<*>> fetchDataThenUpdateItems(
        request: suspend () -> Flow<Result<Model>>,
        map: (Model) -> Ui,
    ): Job = viewModelScope.launch {
        request()
            .singleResult()
            .mapCatching(map)
            .onSuccess { newItem ->
                updateState { s ->
                    s.copy(
                        items = s.items.plusItem(newItem)
                    )
                }
            }
    }

    private suspend fun fetchSecondaryDetails(): Boolean {

        val superTopBottomItem = stateSnapshot.superTopBottomItem

        listOf(
            fetchDataThenUpdateItems(
                request = {
                    vodovozServiceRepository.getSuperTopProducts(
                        stateSnapshot.superTopBottomItem.currentCategoryId
                    )
                },
                map = { products ->
                    superTopBottomItem.withProducts(products.mapToUi())
                }
            ),
            fetchDataThenUpdateItems(
                request = { vodovozServiceRepository.getPromotions() },
                map = { HomeListItem.Promotions(it.toUi()) },
            ),
            fetchDataThenUpdateItems(
                request = { vodovozServiceRepository.getHurryUpBuyProducts() },
                map = { HomeListItem.Products.hurryBuyUp(it.toVodovozSectionUi()) },
            ),
            fetchDataThenUpdateItems(
                request = { vodovozServiceRepository.getNewProducts() },
                map = { HomeListItem.Products.newProducts(it.toVodovozSectionUi()) },
            ),
            fetchDataThenUpdateItems(
                request = { vodovozServiceRepository.getStories() },
                map = { HomeListItem.Stories(it.mapToUi()) },
            ),
            fetchDataThenUpdateItems(
                { vodovozServiceRepository.getViewedProducts() },
                { HomeListItem.Products.viewedProducts(it.toVodovozSectionUi()) }
            )

        ).joinAll()

        return true
    }

    fun fetchHomeDetails(
        onEachIndexed: suspend (Int) -> Unit = {},
    ) = flow {
        emit(fetchPrimaryDetails())
        emit(fetchBottomSheets())
        emit(fetchSecondaryDetails())
    }.withIndex().onEach { indexedValue ->
        onEachIndexed(indexedValue.index)
    }.flowOn(Dispatchers.Default).launchIn(viewModelScope)


    fun refresh() = viewModelScope.launch {
        if (stateSnapshot.uiState is HomeUiState.Loading) return@launch

        updateState { s ->
            s.copy(
                showRefreshIndicator = true,
                sectionUnratedProducts = UnratedProductsSectionUi.Empty
            )
        }

        fetchHomeDetails { index ->
            if (index == 0) {
                updateState { s ->
                    s.copy(showRefreshIndicator = false)
                }
            }
        }.join()

    }


    fun goToProfile() = viewModelScope.launch {
        sendEvent(HomeEvents.GoToProfile)
    }

    fun selectCategory(
        item: HomeListItem.Products.CategoriesWithProductsSection,
        categoryWithProducts: CategoryWithProductsUi,
    ) = viewModelScope.launch {

        val updatedItem = item.copy(
            currentCategoryId = categoryWithProducts.id
        )


        updateState { s ->
            s.copy(items = s.items.plusItem(updatedItem))
        }

        if (categoryWithProducts.items.isNotEmpty()) return@launch

        fetchDataThenUpdateItems(
            request = {
                vodovozServiceRepository.getSuperTopProducts(categoryWithProducts.id)
            },
            map = {
                updatedItem.withProducts(it.mapToUi())
            }
        ).join()
    }

    fun closeSpecialPromotionBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showSpecialPromotionBS = false
            )
        }
    }

    fun navigateToStories(startStory: StoryUi) = viewModelScope.launch {
        val storiesItem = stateSnapshot.items.firstOrNull<HomeListItem.Stories>()

        sendEvent(
            HomeEvents.GoToStories(
                storyId = startStory.id,
                stories = storiesItem?.value ?: return@launch
            )
        )
    }

    fun navigateToPromotionDetails(promotion: PromotionUi) = viewModelScope.launch {
        sendEvent(HomeEvents.GoToPromotionDetails(promotionId = promotion.id))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(HomeEvents.GoToProductDetails(productId = product.id))
    }

    fun handleButtonAction(action: ButtonAction) = viewModelScope.launch {
        sendEvent(HomeEvents.ActivateAction(action))
    }

    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(HomeEvents.GoToSearch)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun closeUnratedProductsBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showUnratedProductsBS = false, showedUnratedProducts = true)
        }
    }

    fun navigateToWriteComment(
        product: UnratedProductUi,
        rating: Float,
    ) = viewModelScope.launch {
        val accountId = accountManager.fetchAccountId()
        if (accountId == null) {
            sendEvent(HomeEvents.GoToProfile)
        } else {
            sendEvent(
                HomeEvents.WriteComment(
                    product.id,
                    product.name,
                    product.detailPicture,
                    rating.roundToInt()
                )
            )
            updateState { s ->
                val sectionUnratedProducts = s.sectionUnratedProducts
                val haveProducts = sectionUnratedProducts.products.isNotEmpty()

                s.copy(
                    showedUnratedProducts = if (haveProducts) s.showedUnratedProducts else true
                )
            }
        }
    }

    fun navigateToPopularCategory(popularCategory: PopularCategoryUi) = viewModelScope.launch {
        if (popularCategory.action == null) {
            sendEvent(HomeEvents.GoToCategoryProductList(popularCategory.id))
        } else {
            sendEvent(HomeEvents.ActivateAction(popularCategory.action))
        }
    }

    fun showAdvertisingBottomSheet(aboutAdvertisingUi: AboutAdvertisingUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentAdvertising = aboutAdvertisingUi,
                showAdvertisingBS = true
            )
        }
    }

    fun closeAdvertisingBottomSheet() {
        updateState { s ->
            s.copy(showAdvertisingBS = false)
        }
    }

    fun showSpeechRecognizer() = viewModelScope.launch {
        sendEvent(HomeEvents.ShowSpeechRecognizer)
    }

    fun activateBannerAction(banner: BannerUi) = viewModelScope.launch {
        sendEvent(HomeEvents.ActivateAction(banner.action))
    }

    fun activateSpecialPromotionAction(action: VodovozAction) = viewModelScope.launch {
        updateState { s -> s.copy(showSpecialPromotionBS = false) }
        delay(100L)
        sendEvent(HomeEvents.ActivateAction(action))
    }


    fun navigateToOrderDetails(order: HomeOrderUi) = viewModelScope.launch {
        sendEvent(HomeEvents.GoToOrderDetails(order.orderId))
    }

    fun navigateByMenuItem(menuItem: MenuItemUi) = viewModelScope.launch {
        fun getWebViewEvent(
            link: AppLink,
        ) = HomeEvents.GoToWebView(link.url, link.title)


        val event = when (menuItem.type) {
            MenuItemTypeUi.History -> HomeEvents.GoToOrdersHistory

            MenuItemTypeUi.Payment -> getWebViewEvent(GlobalAppLinks.aboutPayment)

            MenuItemTypeUi.Delivery -> getWebViewEvent(GlobalAppLinks.aboutDelivery)

            MenuItemTypeUi.None -> {
                return@launch
            }
        }
        sendEvent(event)
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(HomeEvents.GoToProductAnalogs(product.id))
    }

    fun navigateToQrCode() = viewModelScope.launch {
        sendEvent(HomeEvents.GoToQrCode)
    }

    fun showVpnWaring() = viewModelScope.launch {
        sendEvent(HomeEvents.ShowSnackbar(resourcesProvider.getString(R.string.vpn_warning)))
        updateState { s ->
            s.copy(showedVpnWarning = true)
        }
    }

    fun showUnratedProducts() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showUnratedProductsBS = s.sectionUnratedProducts.products.isNotEmpty(),
            )
        }
    }

    fun showExitDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showExitDialog = true)
        }
    }

    fun hideExitDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showExitDialog = false)
        }
    }


    fun closeApplication() = viewModelScope.launch {
        updateState { s -> s.copy(showExitDialog = false) }
        sendEvent(HomeEvents.CloseApp)
    }

    fun noRateProduct(product: UnratedProductUi) = viewModelScope.launch {
        updateState { s ->
            val sectionUnratedProducts = s.sectionUnratedProducts.copy(
                products = s.sectionUnratedProducts.products - product
            )
            val haveProducts = sectionUnratedProducts.products.isNotEmpty()

            s.copy(
                sectionUnratedProducts = sectionUnratedProducts,
                showedUnratedProducts = if (haveProducts) s.showedUnratedProducts else true
            )
        }
        vodovozServiceRepository.removeUnratedProduct(productId = product.id).singleResult()
    }

    fun removeUnratedProduct(productId: Long) = viewModelScope.launch {
        updateState { s ->
            val products = s.sectionUnratedProducts.products
            s.copy(
                sectionUnratedProducts = s.sectionUnratedProducts.copy(
                    products = products - products.filter { it.id == productId }.toSet()
                )
            )
        }
    }

    fun openGooglePlay(appUpdateInfo: AppUpdateInfoUi) = viewModelScope.launch {
        sendEvent(HomeEvents.OpenGooglePlay(appUpdateInfo.playMarketUrl))
    }

    private suspend fun fetchBottomSheets() {
        val popupWindowInfoModel = vodovozServiceRepository.getPopupWindowInfo(
        ).singleResult().getOrNull()
        val unratedProductsSection = vodovozServiceRepository.getUnratedProductsDetails(
        ).singleResult().getOrNull()

        val specialPromotion = popupWindowInfoModel?.specialPromotion?.toUi()


        val updateAppWindow: AppUpdateInfoUi? = popupWindowInfoModel
            ?.appUpdateInfo
            ?.toUi()
            ?.takeIf { appUpdateInfo ->
                compareVersions(BuildConfig.VERSION_NAME, appUpdateInfo.androidVersion) == -1
            }

        updateState { s ->
            s.copy(
                specialPromotion = specialPromotion ?: s.specialPromotion,
                showSpecialPromotionBS = specialPromotion != null && s.specialPromotion == SpecialPromotionUi.Empty && updateAppWindow == null,
                uiState = if (updateAppWindow != null) HomeUiState.AppNeedUpdate(updateAppWindow) else s.uiState,
                sectionUnratedProducts = unratedProductsSection?.toUi() ?: s.sectionUnratedProducts
            )
        }
    }

    @Stable
    sealed class HomeEvents : Event {
        data class GoToPreOrder(val id: Long, val name: String, val detailPicture: String) :
            HomeEvents()

        data object GoToSearch : HomeEvents()

        data object GoToProfile : HomeEvents()
        data object ShowSpeechRecognizer : HomeEvents()
        data object GoToOrdersHistory : HomeEvents()
        data object GoToQrCode : HomeEvents()
        data object CloseApp : HomeEvents()
        data class OpenGooglePlay(val url: String) : HomeEvents()
        data object GoToViewedProductList : HomeEvents()

        data class WriteComment(
            val productId: Long,
            val productName: String,
            val productImage: String,
            val rating: Int,
        ) : HomeEvents()

        data class GoToStories(val storyId: Long, val stories: List<StoryUi>) : HomeEvents()
        data class GoToProductDetails(val productId: Long) : HomeEvents()
        data class GoToPromotionDetails(val promotionId: Long) : HomeEvents()
        data class ActivateAction(val action: BaseVodovozAction) : HomeEvents()
        data class GoToCategoryProductList(val categoryId: Long) : HomeEvents()
        data class GoToOrderDetails(val orderId: Long) : HomeEvents()
        data class GoToWebView(val url: String, val title: String) : HomeEvents()
        data class GoToProductAnalogs(val productId: Long) : HomeEvents()
        data class ShowSnackbar(val message: String) : HomeEvents()
    }

    @Stable
    sealed class HomeUiState {
        data object Success : HomeUiState()
        data object Loading : HomeUiState()
        data object NetworkError : HomeUiState()
        data class AppNeedUpdate(val info: AppUpdateInfoUi) : HomeUiState()
    }

    @Immutable
    data class HomeState(
        val sectionUnratedProducts: UnratedProductsSectionUi = UnratedProductsSectionUi.Empty,
        val specialPromotion: SpecialPromotionUi = SpecialPromotionUi.Empty,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
        val uiState: HomeUiState = HomeUiState.Loading,
        val showSpecialPromotionBS: Boolean = false,
        val showUnratedProductsBS: Boolean = false,
        val showAdvertisingBS: Boolean = false,
        val showRefreshIndicator: Boolean = false,
        val showExitDialog: Boolean = false,
        val showedVpnWarning: Boolean = false,
        val showedUnratedProducts: Boolean = false,
        override val items: List<HomeListItem<*>> = emptyList(),
    ) : ItemsState<HomeListItem<*>, HomeState>() {

        val stories
            get() = items.firstValueOrNull<List<StoryUi>, HomeListItem.Stories>() ?: emptyList()

        val superTopBottomItem
            get() = items.firstOrNull<HomeListItem.Products.CategoriesWithProductsSection>(
                HomeListItem.Positions.BOTTOM_SECTION
            ) ?: HomeListItem.Products.bottomSection(VodovozSectionUi.empty())

        override fun withItems(newItems: List<HomeListItem<*>>): HomeState {
            return copy(items = newItems)
        }


    }

}
