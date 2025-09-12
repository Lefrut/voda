package com.vodovoz.app.feature.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.model.BaseVodovozAction
import com.vodovoz.app.common.model.ButtonAction
import com.vodovoz.app.common.model.DataAllAction
import com.vodovoz.app.common.model.GlobalAppLinks
import com.vodovoz.app.common.model.VodovozAction
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.SpecialPromotionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.toVodovozSectionUi
import com.vodovoz.app.domain.general.model.product.TopAndBottomSectionsModel
import com.vodovoz.app.domain.general.model.promotion.toUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.AppUpdateInfoUi
import com.vodovoz.app.feature.home.model.HomeListItem
import com.vodovoz.app.feature.home.model.HomeOrderUi
import com.vodovoz.app.feature.home.model.MenuItemTypeUi
import com.vodovoz.app.feature.home.model.MenuItemUi
import com.vodovoz.app.feature.home.model.PopularCategoryUi
import com.vodovoz.app.feature.home.model.UnratedProductUi
import com.vodovoz.app.feature.home.model.UnratedProductsSectionUi
import com.vodovoz.app.feature.home.model.compareVersions
import com.vodovoz.app.feature.home.model.getOrNull
import com.vodovoz.app.feature.home.model.getValueOrNull
import com.vodovoz.app.feature.home.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.ItemsState
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import com.vodovoz.app.util.extensions.awaitResultOrNull
import com.vodovoz.app.util.extensions.deferredResult
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
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
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val accountManager: AccountManager,
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
        stateSnapshot.items.getValueOrNull<List<StoryUi>, HomeListItem.Stories>()
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
        val sectionsTopAndBottomDeferred = vodovozServiceRepository.getSuperTop().deferredResult()

        val banners = bannersDeferred.awaitResultOrNull()
        val sectionPopularCategories = sectionPopularCategoriesDeferred.awaitResultOrNull()
        val orderMenu = orderMenuDeferred.awaitResultOrNull()
        val sectionsTopAndBottom = sectionsTopAndBottomDeferred.awaitResultOrNull(
        ) ?: TopAndBottomSectionsModel.Empty
        val topSection = sectionsTopAndBottom.topSection.toUi()
        val bottomSection = sectionsTopAndBottom.bottomSection.toUi()

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
                        add(HomeListItem.Products.topSection(topSection))
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

    private fun <T, Ui : HomeListItem<*>> fetchSectionThenUpdateItems(
        request: suspend () -> Flow<Result<T>>,
        map: (T) -> Ui,
    ): Job = viewModelScope.launch {
        request()
            .singleResult()
            .mapCatching(map)
            .onSuccess { ui ->
                updateState { s ->
                    s.copy(
                        items = s.items + ui
                    )
                }
            }
    }

    private suspend fun fetchSecondaryDetails(): Boolean {
        listOf(
            fetchSectionThenUpdateItems(
                request = { vodovozServiceRepository.getPromotions() },
                map = { HomeListItem.Promotions(it.toUi()) },
            ),
            fetchSectionThenUpdateItems(
                request = { vodovozServiceRepository.getHurryUpBuyProducts() },
                map = { HomeListItem.Products.hurryBuyUp(it.toVodovozSectionUi()) },
            ),
            fetchSectionThenUpdateItems(
                request = { vodovozServiceRepository.getNewProducts() },
                map = { HomeListItem.Products.newProducts(it.toVodovozSectionUi()) },
            ),
            fetchSectionThenUpdateItems(
                request = { vodovozServiceRepository.getStories() },
                map = { HomeListItem.Stories(it.mapToUi()) },
            ),
            fetchSectionThenUpdateItems(
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
        return@flow
    }.withIndex().onEach { indexedValue ->
        onEachIndexed(indexedValue.index)
    }.launchIn(viewModelScope)


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
        item: HomeListItem.Products.CategoryWithProductsSection,
        id: Long,
    ) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                items = s.items.map { homeItem ->
                    if (homeItem.position == item.position && homeItem is HomeListItem.Products.CategoryWithProductsSection) {
                        homeItem.copy(currentCategoryId = id)
                    } else homeItem
                }
            )
        }
    }

    fun closeSpecialPromotionBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showSpecialPromotionBS = false
            )
        }
    }

    fun navigateToStories(startStory: StoryUi) = viewModelScope.launch {
        val storiesItem = stateSnapshot.items.getOrNull<HomeListItem.Stories>()

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
        val event = when (menuItem.type) {
            MenuItemTypeUi.History -> HomeEvents.GoToOrdersHistory
            MenuItemTypeUi.Payment -> with(GlobalAppLinks.aboutPayment) {
                HomeEvents.GoToWebView(url, title)
            }

            MenuItemTypeUi.Delivery -> with(GlobalAppLinks.aboutDelivery) {
                HomeEvents.GoToWebView(url, title)
            }

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
        data class ActivateAction(val action: BaseVodovozAction): HomeEvents()
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
            get() = items.getValueOrNull<List<StoryUi>, HomeListItem.Stories>() ?: emptyList()

        override fun withItems(newItems: List<HomeListItem<*>>): HomeState {
            return copy(items = newItems)
        }


    }

}