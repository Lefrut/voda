package com.vodovoz.app.feature.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.model.ButtonAction
import com.vodovoz.app.common.model.DataAllAction
import com.vodovoz.app.common.model.VodovozAction
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.CategoryWithProductsUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.SpecialPromotionUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withCanViewForAdults
import com.vodovoz.app.design_system.model.withUpdatedCartRecursive
import com.vodovoz.app.design_system.model.withUpdatedFavoritesRecursive
import com.vodovoz.app.design_system.model.withUpdatedLoadingsRecursive
import com.vodovoz.app.domain.general.model.promotion.toUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.AppUpdateInfoUi
import com.vodovoz.app.feature.home.model.HomeOrderUi
import com.vodovoz.app.feature.home.model.MenuItemTypeUi
import com.vodovoz.app.feature.home.model.MenuItemUi
import com.vodovoz.app.feature.home.model.OrderWithMenuUi
import com.vodovoz.app.feature.home.model.PopularCategoryUi
import com.vodovoz.app.feature.home.model.UnratedProductUi
import com.vodovoz.app.feature.home.model.UnratedProductsSectionUi
import com.vodovoz.app.feature.home.model.compareVersions
import com.vodovoz.app.feature.home.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
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
) : MviViewModel<HomeFlowViewModel.HomeState, HomeFlowViewModel.HomeEvents>(HomeState()) {

    suspend fun listenCanView(): Unit =
        userPreferencesRepository.canViewAdultProducts.combine(
            state.map { s -> s.uiState }
        ) { canView, _ -> canView }
            .shareIn(viewModelScope, SharingStarted.Lazily, 1).collect { canView ->
                updateState { s ->
                    val currentCategoryWithProducts = listOf(
                        stateSnapshot.currentCategoryWithProducts
                    ).withCanViewForAdults(canView).firstOrNull()

                    s.copy(
                        currentCategoryWithProducts = currentCategoryWithProducts
                            ?: s.currentCategoryWithProducts,
                        sectionTop = s.sectionTop.withItems {
                            withCanViewForAdults(canView)
                        },
                        sectionBottom = s.sectionBottom.withItems {
                            withCanViewForAdults(canView)
                        },
                        sectionNewProducts = s.sectionNewProducts.withItems {
                            withCanViewForAdults(canView)
                        },
                        sectionHurryUpBuyProducts = s.sectionHurryUpBuyProducts.withItems {
                            withCanViewForAdults(canView)
                        },
                        sectionViewedProducts = s.sectionViewedProducts.withItems {
                            withCanViewForAdults(canView)
                        }
                    )
                }

            }

    suspend fun listenStories(): Unit = _state.map { stateSnapshot.stories }
        .distinctUntilChanged()
        .combine(userPreferencesRepository.viewedStoryIds) { _, p2 ->
            p2
        }.shareIn(viewModelScope, SharingStarted.Lazily, 1).collect { storyIds ->
            _state.update { s ->
                s.copy(
                    stories = s.stories.map { story ->
                        if (storyIds.contains(story.id)) story.copy(viewed = true) else story
                    }.sortedBy { it.viewed }
                )
            }
        }

    suspend fun listenLoadingProducts(): Unit =
        _state.map { pagingState -> pagingState.uiState }.distinctUntilChanged().combine(
            cartManager.blockedProductsState
        ) { _, productIds ->
            productIds
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
            .collect { blockedProductsIds ->
                updateState { s ->

                    val currentCategoryWithProducts = listOf(
                        stateSnapshot.currentCategoryWithProducts
                    ).withUpdatedLoadingsRecursive(blockedProductsIds).firstOrNull()

                    s.copy(
                        currentCategoryWithProducts = currentCategoryWithProducts
                            ?: s.currentCategoryWithProducts,
                        sectionTop = s.sectionTop.withItems {
                            withUpdatedLoadingsRecursive(
                                blockedProductsIds
                            )
                        },
                        sectionBottom = s.sectionBottom.withItems {
                            withUpdatedLoadingsRecursive(
                                blockedProductsIds
                            )
                        },
                        sectionNewProducts = s.sectionNewProducts.withItems {
                            withUpdatedLoadingsRecursive(
                                blockedProductsIds
                            )
                        },
                        sectionHurryUpBuyProducts = s.sectionHurryUpBuyProducts.withItems {
                            withUpdatedLoadingsRecursive(
                                blockedProductsIds
                            )
                        },
                        sectionViewedProducts = s.sectionViewedProducts.withItems {
                            withUpdatedLoadingsRecursive(blockedProductsIds)
                        }
                    )
                }
            }

    suspend fun listenCart(): Unit =
        _state.map { stateSnapshot.uiState }.distinctUntilChanged().combine(
            cartManager.observeCarts()
        ) { _, cartMap ->
            cartMap
        }.collect { cartMap ->
            _state.update { s ->

                val currentCategoryWithProducts =
                    stateSnapshot.currentCategoryWithProducts.withUpdatedCartRecursive(cartMap) as? CategoryWithProductsUi

                s.copy(
                    currentCategoryWithProducts = currentCategoryWithProducts
                        ?: s.currentCategoryWithProducts,
                    sectionTop = s.sectionTop.withItems { withUpdatedCartRecursive(cartMap) },
                    sectionBottom = s.sectionBottom.withItems { withUpdatedCartRecursive(cartMap) },
                    sectionNewProducts = s.sectionNewProducts.withItems {
                        withUpdatedCartRecursive(cartMap)
                    },
                    sectionHurryUpBuyProducts = s.sectionHurryUpBuyProducts.withItems {
                        withUpdatedCartRecursive(cartMap)
                    },
                    sectionViewedProducts = s.sectionViewedProducts.withItems {
                        withUpdatedCartRecursive(cartMap)
                    },
                )
            }
        }


    suspend fun listenFavorites(mainScope: CoroutineScope) = mainScope.launch {
        _state.map { it.uiState }.distinctUntilChanged()
            .combine(likeManager.observeLikes()) { uiState, favorites ->
                uiState to favorites
            }.stateIn(viewModelScope).collect { (uiState, favorites) ->

                if (uiState != HomeUiState.Success) return@collect

                val sectionTopDeferred =
                    async(Dispatchers.Default) {
                        stateSnapshot.sectionTop.withItems {
                            withUpdatedFavoritesRecursive(favorites)
                        }
                    }
                val sectionBottomDeferred =
                    async(Dispatchers.Default) {
                        stateSnapshot.sectionBottom.withItems {
                            withUpdatedFavoritesRecursive(favorites)
                        }
                    }
                val sectionViewedProductsDeferred =
                    async(Dispatchers.Default) {
                        stateSnapshot.sectionViewedProducts.withItems {
                            withUpdatedFavoritesRecursive(favorites)
                        }
                    }
                val sectionNewProductsDeferred =
                    async(Dispatchers.Default) {
                        stateSnapshot.sectionNewProducts.withItems {
                            withUpdatedFavoritesRecursive(favorites)
                        }
                    }
                val sectionHurryUpBuyProducts =
                    stateSnapshot.sectionHurryUpBuyProducts.withItems {
                        withUpdatedFavoritesRecursive(favorites)
                    }


                val currentCategoryWithProducts = listOf(
                    stateSnapshot.currentCategoryWithProducts
                ).withUpdatedFavoritesRecursive(favorites).firstOrNull()
                    ?: stateSnapshot.currentCategoryWithProducts

                val sectionTop = sectionTopDeferred.await()
                val sectionBottom = sectionBottomDeferred.await()
                val sectionViewedProducts = sectionViewedProductsDeferred.await()
                val sectionNewProducts = sectionNewProductsDeferred.await()

                debugLog { "listenFavorites: HomeViewModel" }

                _state.update { s ->
                    s.copy(
                        sectionTop = sectionTop,
                        sectionBottom = sectionBottom,
                        sectionViewedProducts = sectionViewedProducts,
                        sectionNewProducts = sectionNewProducts,
                        sectionHurryUpBuyProducts = sectionHurryUpBuyProducts,
                        currentCategoryWithProducts = currentCategoryWithProducts
                    )
                }

            }
    }

    private suspend fun fetchPrimaryDetails(): Boolean {
        val bannersDeferred = viewModelScope.async {
            vodovozServiceRepository.getBanners().singleResult()
        }
        val storiesDeferred = viewModelScope.async {
            vodovozServiceRepository.getStories().singleResult()
        }
        val sectionPopularCategoriesDeferred = viewModelScope.async {
            vodovozServiceRepository.getPopularCategories().singleResult()
        }
        val orderMenuDeferred = viewModelScope.async {
            vodovozServiceRepository.getOrderMenu().singleResult()
        }
        val sectionsTopAndBottomDeferred = viewModelScope.async {
            vodovozServiceRepository.getSuperTop().singleResult()
        }

        val banners = bannersDeferred.await().getOrNull()
        val stories = storiesDeferred.await().getOrNull()
        val sectionPopularCategories = sectionPopularCategoriesDeferred.await().getOrNull()
        val orderMenu = orderMenuDeferred.await().getOrNull()
        val sectionsTopAndBottom = sectionsTopAndBottomDeferred.await().getOrNull()

        if (banners != null && sectionPopularCategories != null && orderMenu != null && sectionsTopAndBottom != null) {
            val topSection = sectionsTopAndBottom.topSection.toUi(
                mapItems = { items -> items.map { it.toUi() } }
            )

            _state.update { s ->
                s.copy(
                    sectionPopularCategories = sectionPopularCategories.toUi { items -> items.map { it.toUi() } },
                    sectionTop = topSection,
                    sectionBottom = sectionsTopAndBottom.bottomSection.toUi { items -> items.map { it -> it.toUi() } },
                    currentCategoryWithProducts = topSection.items.firstOrNull()
                        ?: CategoryWithProductsUi.Empty,
                    orderWithMenu = orderMenu.toUi(),
                    banners = banners.mapToUi(),
                    stories = stories?.mapToUi() ?: emptyList(),
                    uiState = HomeUiState.Success,
                )
            }
        } else {
            _state.update { s -> s.copy(uiState = HomeUiState.NetworkError) }
            return false
        }
        return true
    }

    private suspend fun fetchSecondaryDetails(): Boolean {
        val sectionPromotionsDeferred =
            viewModelScope.async { vodovozServiceRepository.getPromotions().singleResult() }
        val sectionHurryUpBuyProductsDeferred =
            viewModelScope.async { vodovozServiceRepository.getHurryUpBuyProducts().singleResult() }
        val sectionNewProductsDeferred =
            viewModelScope.async { vodovozServiceRepository.getNewProducts().singleResult() }

        sectionPromotionsDeferred.await().onSuccess { value ->
            _state.update { s -> s.copy(sectionPromotions = value.toUi()) }
        }.onFailure { return false }

        sectionHurryUpBuyProductsDeferred.await().onSuccess { value ->
            _state.update { s -> s.copy(sectionHurryUpBuyProducts = value.toUi()) }
        }.onFailure { return false }


        sectionNewProductsDeferred.await().onSuccess { value ->
            _state.update { s -> s.copy(sectionNewProducts = value.toUi()) }
        }.onFailure { return false }

        return true
    }

    private suspend fun fetchOptionalDetails(): Boolean {
        val viewedProductsDeferred = viewModelScope.async {
            vodovozServiceRepository.getViewedProducts().singleResult()
        }

        val popupWindowsInfoDeferred = viewModelScope.async {
            vodovozServiceRepository.getPopupWindowInfo().singleResult()
        }

        val unratedProductsSectionDeferred = viewModelScope.async {
            vodovozServiceRepository.getUnratedProductsDetails().singleResult()
        }


        val sectionViewedProducts =
            viewedProductsDeferred.await().getOrNull()?.toUi()

        val popupWindowInfoModel = popupWindowsInfoDeferred.await().getOrNull()
        val specialPromotion =
            popupWindowInfoModel?.specialPromotion?.toUi()
        val sectionUnratedProducts = unratedProductsSectionDeferred.await().getOrNull()

        val updateAppWindow: AppUpdateInfoUi? =
            popupWindowInfoModel?.appUpdateInfo?.toUi()?.takeIf { appUpdateInfo ->
                compareVersions(BuildConfig.VERSION_NAME, appUpdateInfo.androidVersion) == -1
            }

        _state.update { s ->
            s.copy(
                sectionViewedProducts = sectionViewedProducts ?: s.sectionViewedProducts,
                specialPromotion = specialPromotion ?: s.specialPromotion,
                showSpecialPromotionBS = specialPromotion != null && s.specialPromotion == SpecialPromotionUi.Empty && updateAppWindow == null,
                sectionUnratedProducts = sectionUnratedProducts?.toUi() ?: s.sectionUnratedProducts,
                uiState = if (updateAppWindow != null) HomeUiState.AppNeedUpdate(updateAppWindow) else s.uiState
            )
        }

        return sectionViewedProducts != null
    }


    fun fetchHomeDetails(
        onFirstImportantResult: suspend () -> Unit = {},
    ) = flow {
        _state.update { s ->
            s.copy(uiState = HomeUiState.Loading)
        }
        emit(fetchPrimaryDetails())
        emit(fetchSecondaryDetails())
        emit(fetchOptionalDetails())
        return@flow
    }.withIndex().onEach { indexedValue ->
        if (indexedValue.index == 0) {
            onFirstImportantResult()
        }
    }.launchIn(viewModelScope)


    fun refresh() = viewModelScope.launch {
        if (stateSnapshot.uiState is HomeUiState.Loading) return@launch

        _state.update { s ->
            s.copy(
                showRefreshIndicator = true,
                sectionUnratedProducts = UnratedProductsSectionUi.Empty
            )
        }

        fetchHomeDetails {
            _state.update { s ->
                s.copy(showRefreshIndicator = false)
            }
        }

    }


    fun goToProfile() {
        viewModelScope.launch {
            sendEvent(HomeEvents.GoToProfile)
        }
    }

    fun selectCategory(categoryWithProductsUi: CategoryWithProductsUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentCategoryWithProducts = categoryWithProductsUi
            )
        }
        sendEvent(HomeEvents.ScrollTopProductsToStart)
    }

    fun closeSpecialPromotionBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showSpecialPromotionBS = false
            )
        }
    }

    fun navigateToStories(startStory: StoryUi) = viewModelScope.launch {
        sendEvent(
            HomeEvents.GoToStories(
                storyId = startStory.id,
                stories = stateSnapshot.stories
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
        sendEvent(HomeEvents.ActivateButtonAction(action))
    }

    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(HomeEvents.GoToSearch)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun closeUnratedProductsBottomSheet() = viewModelScope.launch {
        _state.update { s ->
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
            _state.update { s ->
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
            sendEvent(HomeEvents.ActivateDataAllAction(popularCategory.action))
        }
    }

    fun showAdvertisingBottomSheet(aboutAdvertisingUi: AboutAdvertisingUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentAdvertising = aboutAdvertisingUi,
                showAdvertisingBS = true
            )
        }
    }

    fun closeAdvertisingBottomSheet() {
        _state.update { s ->
            s.copy(showAdvertisingBS = false)
        }
    }

    fun showSpeechRecognizer() = viewModelScope.launch {
        sendEvent(HomeEvents.ShowSpeechRecognizer)
    }

    fun activateBannerAction(banner: BannerUi) = viewModelScope.launch {
        sendEvent(HomeEvents.ActivateVodovozAction(banner.action))
    }

    fun activateSpecialPromotionAction(action: VodovozAction) = viewModelScope.launch {
        _state.update { s -> s.copy(showSpecialPromotionBS = false) }
        delay(100L)
        sendEvent(HomeEvents.ActivateVodovozAction(action))
    }


    fun navigateToOrderDetails(order: HomeOrderUi) = viewModelScope.launch {
        sendEvent(HomeEvents.GoToOrderDetails(order.orderId))
    }

    fun navigateByMenuItem(menuItem: MenuItemUi) = viewModelScope.launch {
        val event = when (menuItem.type) {
            MenuItemTypeUi.History -> HomeEvents.GoToOrdersHistory
            MenuItemTypeUi.Payment -> HomeEvents.GoToWebView(
                VodovozWebConfig.ABOUT_PAYMENT_URL, menuItem.title
            )

            MenuItemTypeUi.Delivery -> HomeEvents.GoToWebView(
                VodovozWebConfig.ABOUT_DELIVERY_URL, menuItem.title
            )

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
        _state.update { s ->
            s.copy(showedVpnWarning = true)
        }
    }

    fun showUnratedProducts() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showUnratedProductsBS = s.sectionUnratedProducts.products.isNotEmpty(),
            )
        }
    }

    fun showExitDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showExitDialog = true)
        }
    }

    fun hideExitDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showExitDialog = false)
        }
    }


    fun closeApplication() = viewModelScope.launch {
        _state.update { s -> s.copy(showExitDialog = false) }
        sendEvent(HomeEvents.CloseApp)
    }

    fun noRateProduct(product: UnratedProductUi) = viewModelScope.launch {
        _state.update { s ->
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

    fun navigateToViewedProducts() = viewModelScope.launch {
        sendEvent(HomeEvents.GoToViewedProductList)
    }

    fun removeUnratedProduct(productId: Long) = viewModelScope.launch {
        _state.update { s ->
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

    @Stable
    sealed class HomeEvents : Event {
        data class GoToPreOrder(val id: Long, val name: String, val detailPicture: String) :
            HomeEvents()

        data object GoToSearch : HomeEvents()

        data object GoToProfile : HomeEvents()
        data object ScrollTopProductsToStart : HomeEvents()
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
        data class ActivateButtonAction(val action: ButtonAction) : HomeEvents()
        data class GoToCategoryProductList(val categoryId: Long) : HomeEvents()
        data class ActivateDataAllAction(val action: DataAllAction) : HomeEvents()
        data class ActivateVodovozAction(val action: VodovozAction) : HomeEvents()
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
        val banners: List<BannerUi> = emptyList(),
        val stories: List<StoryUi> = emptyList(),
        val orderWithMenu: OrderWithMenuUi = OrderWithMenuUi.Empty,
        val sectionPromotions: SectionUi<PromotionUi> = SectionUi.empty(),
        val sectionPopularCategories: SectionUi<PopularCategoryUi> = SectionUi.empty(),
        val sectionNewProducts: SectionUi<ProductUi> = SectionUi.empty(),
        val sectionHurryUpBuyProducts: SectionUi<ProductUi> = SectionUi.empty(),
        val sectionTop: SectionUi<CategoryWithProductsUi> = SectionUi.empty(),
        val currentCategoryWithProducts: CategoryWithProductsUi = CategoryWithProductsUi.Empty,
        val sectionBottom: SectionUi<CategoryWithProductsUi> = SectionUi.empty(),
        val sectionViewedProducts: SectionUi<ProductUi> = SectionUi.empty(),
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
    ) : State

}