package com.vodovoz.app.feature.product_details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.about_product.AboutProductManager
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.BrandCategoryItemUi
import com.vodovoz.app.design_system.model.BuyButtonUi
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.CommentUi
import com.vodovoz.app.design_system.model.ForAdultsUi
import com.vodovoz.app.design_system.model.PriceUi
import com.vodovoz.app.design_system.model.ProductDetailsButtonsUi
import com.vodovoz.app.design_system.model.ProductDetailsTabUi
import com.vodovoz.app.design_system.model.ProductDetailsUi
import com.vodovoz.app.design_system.model.ProductMediaUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.product_details.model.PresentInfoUi
import com.vodovoz.app.feature.product_details.model.toUi
import com.vodovoz.app.util.calculateProductPrice
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class ProductDetailsFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val aboutProductManager: AboutProductManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uiStateListener = MutableStateFlow(ProductDetailsState())
    private val state
        get() = uiStateListener.value



    init {
        savedStateHandle.get<Long>("productId")?.let {
            uiStateListener.update { s ->
                s.copy(productDetails = s.productDetails.copy(id = it))
            }
        }
        fetchProductDetails()
    }


    private val eventListener = MutableSharedFlow<ProductDetailsEvents>(replay = 0)
    fun observeEvent() = eventListener.asSharedFlow()

    fun observeUiState() = uiStateListener.asStateFlow()

    suspend fun listenLoadingsProduct() = uiStateListener.combine(
        cartManager.blockedProductsState
    ) { _, blockedProducts ->
        blockedProducts
    }.collectLatest { blockedProducts ->
        uiStateListener.update { s ->
            val sectionSimilarProducts = s.sectionSimilarProducts
            val sectionAccessory = s.sectionAccessory
            val productDetails = s.productDetails

            s.copy(
                buttonIsLoading = productDetails.id in blockedProducts,
                sectionAccessory = sectionAccessory.withUpdatedLoading(blockedProducts),
                sectionSimilarProducts = sectionSimilarProducts.withUpdatedLoading(blockedProducts),
            )
        }
    }

    suspend fun listenCart() = uiStateListener.combine(
        cartManager.observeCarts()
    ) { _, cartMap ->
        cartMap
    }.collectLatest { cartMap ->
        uiStateListener.update { s ->
            val sectionSimilarProducts = s.sectionSimilarProducts
            val sectionAccessory = s.sectionAccessory
            val productDetails = s.productDetails
            val productCartQuantity = cartMap.getOrDefault(
                productDetails.id,
                0
            )

            s.copy(
                productDetails = productDetails.copy(
                    cartQuantity = productCartQuantity
                ),
                sectionAccessory = sectionAccessory.withUpdatedCart(cartMap),
                sectionSimilarProducts = sectionSimilarProducts.withUpdatedCart(cartMap),
                totalPrice = calculateProductPrice(
                    productCartQuantity,
                    productDetails.prices
                ).roundToInt()

            )
        }
    }

    suspend fun listenFavorites() = uiStateListener.combine(
        likeManager.observeLikes()
    ) { _, favorites ->
        favorites
    }.collectLatest { favoritesMap ->
        uiStateListener.update { s ->

            val sectionSimilarProducts = s.sectionSimilarProducts
            val sectionAccessory = s.sectionAccessory
            val productDetails = s.productDetails

            s.copy(
                productDetails = productDetails.copy(
                    isFavorite = favoritesMap.getOrDefault(
                        productDetails.id,
                        productDetails.isFavorite
                    )
                ),
                sectionSimilarProducts = sectionSimilarProducts.copy(
                    items = sectionSimilarProducts.items.withUpdatedFavorites(favoritesMap)
                ),
                sectionAccessory = sectionAccessory.copy(
                    items = sectionAccessory.items.withUpdatedFavorites(favoritesMap)
                )

            )
        }
    }

    suspend fun listenCartUpdates() = cartManager.observeUpdateCartList().onEach { update ->
        if (update) {
            vodovozServiceRepository.getPresentInfo().singleResult().onSuccess { presentInfo ->
                uiStateListener.update { s ->
                    s.copy(presentInfo = presentInfo.toUi())
                }
            }
        }
    }.collect()

    private fun fetchProductDetails() =
        vodovozServiceRepository.getProductDetails(state.productDetails.id)
            .combine(vodovozServiceRepository.getPresentInfo()) { p1, p2 ->
                p1 to p2
            }
            .onEach { (productDetailsScreenResult, presentInfoResult) ->
                productDetailsScreenResult.onSuccess { productDetailsScreenModel ->
                    val moreProducts = productDetailsScreenModel.moreProducts
                    val canViewAdultProducts = userPreferencesRepository.getCanViewAdultProducts()
                    val forAdults = productDetailsScreenModel.details.forAdultsModel?.toUi()

                    uiStateListener.update { s ->
                        s.copy(
                            comments = productDetailsScreenModel.comments.mapToUi(),
                            productDetails = productDetailsScreenModel.details.toUi(),
                            sectionAccessory = moreProducts.sectionAccessory.toUi { list ->
                                val uiList = list.map { productModel -> productModel.toUi() }
                                uiList.take(list.size - (list.size % 2))

                            },
                            sectionSimilarProducts = moreProducts.sectionSimilar.toUi { list ->
                                val uiList = list.map { productModel -> productModel.toUi() }
                                uiList.take(list.size - (list.size % 2))
                            },
                            buttons = productDetailsScreenModel.buttons.toUi(),
                            tabs = productDetailsScreenModel.tabs.map { it.toUi() },
                            uiState = if (forAdults != null && !canViewAdultProducts) ProductDetailsUiState.ForAdults(
                                forAdults
                            ) else ProductDetailsUiState.Success,
                            presentInfo = presentInfoResult.getOrNull()?.toUi() ?: s.presentInfo
                        )
                    }

                    val productDetails = state.productDetails

                    aboutProductManager.updateInfo(
                        tabs = state.tabs,
                        characteristicBlockList = productDetails.characteristics,
                        documents = productDetails.documents,
                        fullDescription = productDetails.detailInfo
                    )

                }.onFailure {
                    uiStateListener.update { s ->
                        s.copy(uiState = ProductDetailsUiState.ProductNotFound)
                    }
                }
            }.launchIn(viewModelScope)

    fun incrementCart() = viewModelScope.launch {
        val productDetails = state.productDetails
        cartManager.change(productDetails.id, productDetails.cartQuantity + 1)
    }

    fun decrementCart() = viewModelScope.launch {
        val productDetails = state.productDetails
        cartManager.change(productDetails.id, productDetails.cartQuantity - 1)
    }

    fun showOrHideDetailText() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showDetailText = !s.showDetailText
            )
        }
    }

    fun showAllProperties() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showAllProperties = true
            )
        }
    }

    fun changeFloatingButton(show: Boolean) = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                hideFloatingButton = show
            )
        }
    }

    fun hideMultiBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showMultiBottomSheet = false
            )
        }
    }

    fun showMultiBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            val cartQuantity = s.productDetails.cartQuantity
            s.copy(
                showMultiBottomSheet = true,
                multiProductQuantity = cartQuantity.coerceAtLeast(1),
                multiProductTotalPrice = calculateProductPrice(
                    cartQuantity.coerceAtLeast(1),
                    s.productDetails.prices
                ).toInt()
            )
        }
    }

    fun showPresentBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showPresentBottomSheet = true
            )
        }
    }

    fun hidePresentBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showPresentBottomSheet = false
            )
        }
    }

    fun hidePresentBlockBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showPresentBlockBottomSheet = false
            )
        }
    }

    fun showPresentBlockBottomSheet() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                showPresentBlockBottomSheet = true
            )
        }
    }

    fun navigateToAboutProduct() = viewModelScope.launch {

        val viewState = uiStateListener.value

        eventListener.emit(
            ProductDetailsEvents.GoToAboutProduct(
                viewState.productDetails.id,
                viewState.productDetails.prices,
                viewState.buttons.analogButton,
                viewState.productDetails.isAvailable
            )
        )
    }

    fun setupProductDetails(productId: Long) = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                productDetails = s.productDetails.copy(id = productId),
                uiState = ProductDetailsUiState.Loading
            )
        }
        fetchProductDetails()
    }

    fun showAllComments() = viewModelScope.launch {
        val productDetails = state.productDetails
        eventListener.emit(
            ProductDetailsEvents.GoToProductComments(
                productDetails.id,
                productDetails.name,
                productDetails.detailPicture
            )
        )
    }

    fun navigateToProductAnalogs() = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToProductAnalogs(state.productDetails.id))
    }

    fun navigateToPreOrder() = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToPreOrder(state.productDetails.id))
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoBack)
    }

    fun navigateToSearch(query: String) = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToSearchProductList(query))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToProductDetails(product.id))
    }

    fun changeFavorite() = viewModelScope.launch {
        val productDetails = uiStateListener.value.productDetails
        likeManager.changeFavorite(productDetails.id, !productDetails.isFavorite)
    }


    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToCategory(categoryItem: BrandCategoryItemUi) = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToCategoryProductList(categoryItem.data.id.toLong()))
    }

    fun share() = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.Share(uiStateListener.value.productDetails.shareUrlText))
    }

    fun copyArticleNumber() = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.Copy(uiStateListener.value.productDetails.articleNumber))
    }

    fun navigateToDetailMedia(media: ProductMediaUi) = viewModelScope.launch {
        val productDetails = uiStateListener.value.productDetails
        val mediaList = productDetails.mediaList

        eventListener.emit(
            ProductDetailsEvents.GoToDetailMedia(media, mediaList)
        )
    }

    fun navigateToBrandProducts(brandItem: BrandCategoryItemUi) = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToBrandProducts(brandItem.data.id.toLong()))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(ProductDetailsEvents.GoToProductAnalogs(product.id))
    }

    fun addProductWithGift(buyButton: BuyButtonUi) = viewModelScope.launch {
        cartManager.add(buyButton.productId, buyButton.moreProductId)
        uiStateListener.update { s ->
            s.copy(
                showPresentBottomSheet = false,
                showPresentBlockBottomSheet = false
            )
        }
    }

    fun navigateToWriteComment() = viewModelScope.launch {
        val productDetails = uiStateListener.value.productDetails
        eventListener.emit(
            ProductDetailsEvents.GoToWriteComment(
                productDetails.id,
                productDetails.detailPicture,
                productDetails.name,
                0
            )
        )
    }

    fun saveMultiProductChoice() = viewModelScope.launch {
        val state = uiStateListener.value
        val productDetails = state.productDetails
        cartManager.change(productDetails.id, state.multiProductQuantity)
        uiStateListener.update { s ->
            s.copy(showMultiBottomSheet = false)
        }
    }

    fun changeMultiProductQuantity(newMultiProductQuantity: Int) = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(
                multiProductQuantity = newMultiProductQuantity,
                multiProductTotalPrice = calculateProductPrice(
                    newMultiProductQuantity,
                    s.productDetails.prices
                ).toInt()
            )
        }
    }

    fun decrementMultiProduct() = viewModelScope.launch {
        uiStateListener.update { s ->
            val newMultiProductQuantity = (s.multiProductQuantity - 1).coerceAtLeast(1)
            s.copy(
                multiProductQuantity = newMultiProductQuantity,
                multiProductTotalPrice = calculateProductPrice(
                    newMultiProductQuantity,
                    s.productDetails.prices
                ).toInt()
            )
        }
    }

    fun incrementMultiProduct() = viewModelScope.launch {
        uiStateListener.update { s ->
            val newMultiProductQuantity = s.multiProductQuantity + 1

            s.copy(
                multiProductQuantity = newMultiProductQuantity,
                multiProductTotalPrice = calculateProductPrice(
                    newMultiProductQuantity,
                    s.productDetails.prices
                ).toInt()
            )
        }
    }

    fun setCanViewAdultProducts() = viewModelScope.launch {
        uiStateListener.update { s ->
            s.copy(uiState = ProductDetailsUiState.Success)
        }
        userPreferencesRepository.setCanViewAdultProducts(true)
    }


    sealed class ProductDetailsEvents : Event {
        data class GoToPreOrder(val id: Long) : ProductDetailsEvents()

        data object GoToProfile : ProductDetailsEvents()

        data object GoToCart : ProductDetailsEvents()
        data class GoToAboutProduct(
            val productId: Long,
            val prices: List<PriceUi>,
            val analogButton: ColorfulButtonUi?,
            val isAvailable: Boolean,
        ) :
            ProductDetailsEvents()

        data object GoBack : ProductDetailsEvents()
        data class Share(val text: String) : ProductDetailsEvents()

        data class GoToProductComments(
            val productId: Long,
            val productName: String,
            val productImage: String,
        ) : ProductDetailsEvents()

        data class GoToProductAnalogs(val productId: Long) : ProductDetailsEvents()
        data class GoToSearch(val query: String) : ProductDetailsEvents()
        data class GoToProductDetails(val productId: Long) : ProductDetailsEvents()
        data class GoToCategoryProductList(val categoryId: Long) : ProductDetailsEvents()
        data class GoToSearchProductList(val query: String) : ProductDetailsEvents()
        data class Copy(val text: String) : ProductDetailsEvents()
        data class GoToDetailMedia(val media: ProductMediaUi, val mediaList: List<ProductMediaUi>) :
            ProductDetailsEvents()

        data class GoToBrandProducts(val brandId: Long) : ProductDetailsEvents()

        data class GoToWriteComment(
            val id: Long,
            val detailPicture: String,
            val name: String,
            val rating: Int,
        ) : ProductDetailsEvents()
    }


    @Immutable
    data class ProductDetailsState(
        val showDetailText: Boolean = false,
        val showAllProperties: Boolean = false,
        val buttonIsLoading: Boolean = false,
        val hideFloatingButton: Boolean = true,

        val productDetails: ProductDetailsUi = ProductDetailsUi.Empty,
        val comments: List<CommentUi> = emptyList(),
        val buttons: ProductDetailsButtonsUi = ProductDetailsButtonsUi.Empty,
        val tabs: List<ProductDetailsTabUi> = emptyList(),
        val sectionSimilarProducts: SectionUi<ProductUi> = SectionUi.empty(),
        val sectionAccessory: SectionUi<ProductUi> = SectionUi.empty(),
        val uiState: ProductDetailsUiState = ProductDetailsUiState.Loading,
        val totalPrice: Int = 0,
        val showMultiBottomSheet: Boolean = false,
        val showPresentBottomSheet: Boolean = false,
        val showPresentBlockBottomSheet: Boolean = false,
        val presentInfo: PresentInfoUi = PresentInfoUi.Empty,

        val multiProductQuantity: Int = 1,
        val multiProductTotalPrice: Int = productDetails.firstPrice.price.toInt(),
    ) : State

    @Stable
    sealed class ProductDetailsUiState {
        data object Loading : ProductDetailsUiState()
        data object Success : ProductDetailsUiState()
        data object ProductNotFound : ProductDetailsUiState()
        data class ForAdults(val forAdultsUi: ForAdultsUi) : ProductDetailsUiState()
    }
}