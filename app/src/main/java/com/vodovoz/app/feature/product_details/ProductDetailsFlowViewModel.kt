package com.vodovoz.app.feature.product_details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.about_product.AboutProductManager
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
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
import com.vodovoz.app.design_system.model.VodovozSectionUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.toVodovozSectionUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.product_details.model.PresentInfoUi
import com.vodovoz.app.feature.product_details.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.ItemsState
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import com.vodovoz.app.util.calculateProductPrice
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ProductDetailsFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val aboutProductManager: AboutProductManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val accountManager: AccountManager,
    savedStateHandle: SavedStateHandle,
) : ProductsMviViewModel<VodovozSectionUi<ProductUi>, ProductDetailsFlowViewModel.ProductDetailsState, ProductDetailsFlowViewModel.ProductDetailsEvents>(
    state = ProductDetailsState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    init {
        savedStateHandle.get<Long>("productId")?.let {
            _state.update { s ->
                s.copy(productDetails = s.productDetails.copy(id = it))
            }
        }
        fetchProductDetails()
    }

    fun listenProductDetailsUpdates(scope: CoroutineScope) {
        cartManager.observeCarts()
            .onEach { cart ->
                updateState { s ->
                    s.copy(
                        productDetails = s.productDetails.copy(
                            cartQuantity = cart.getOrDefault(
                                s.productDetails.id,
                                s.productDetails.cartQuantity
                            )
                        )
                    )
                }
            }
            .launchIn(scope)

        cartManager.blockedProductsState
            .onEach { blocked ->
                updateState { s ->
                    s.copy(
                        buttonIsLoading = s.productDetails.id in blocked
                    )
                }
            }
            .launchIn(scope)

        likeManager.observeLikes()
            .onEach { likes ->
                updateState { s ->
                    s.copy(
                        productDetails = s.productDetails.copy(
                            isFavorite = likes.getOrDefault(
                                s.productDetails.id,
                                s.productDetails.isFavorite
                            )
                        )
                    )
                }
            }
            .launchIn(scope)
    }

    suspend fun listenCartUpdates() = cartManager.observeUpdateCartList().onEach { update ->
        if (update) {
            vodovozServiceRepository.getPresentInfo().singleResult().onSuccess { presentInfo ->
                _state.update { s ->
                    s.copy(presentInfo = presentInfo.toUi())
                }
            }
        }
    }.collect()

    fun fetchProductDetails() =
        vodovozServiceRepository.getProductDetails(stateSnapshot.productDetails.id)
            .combine(vodovozServiceRepository.getPresentInfo()) { p1, p2 ->
                p1 to p2
            }
            .onEach { (productDetailsScreenResult, presentInfoResult) ->
                productDetailsScreenResult.onSuccess { productDetailsScreenModel ->
                    val moreProducts = productDetailsScreenModel.moreProducts
                    val canViewAdultProducts = userPreferencesRepository.getCanViewAdultProducts()
                    val forAdults = productDetailsScreenModel.details.forAdultsModel?.toUi()

                    _state.update { s ->
                        s.copy(
                            comments = productDetailsScreenModel.comments.mapToUi(),
                            productDetails = productDetailsScreenModel.details.toUi(),
                            items = moreProducts.map { section ->
                                section.toVodovozSectionUi()
                            },
                            buttons = productDetailsScreenModel.buttons.toUi(),
                            tabs = productDetailsScreenModel.tabs.map { it.toUi() },
                            uiState = if (forAdults != null && !canViewAdultProducts) ProductDetailsUiState.ForAdults(
                                forAdults
                            ) else ProductDetailsUiState.Success,
                            presentInfo = presentInfoResult.getOrNull()?.toUi() ?: s.presentInfo
                        )
                    }

                }.onFailure {
                    _state.update { s ->
                        s.copy(uiState = ProductDetailsUiState.Error)
                    }
                }
            }.launchIn(viewModelScope)

    fun incrementCart() = viewModelScope.launch {
        val productDetails = stateSnapshot.productDetails
        cartManager.change(productDetails.id, productDetails.cartQuantity + 1)
    }

    fun decrementCart() = viewModelScope.launch {
        val productDetails = stateSnapshot.productDetails
        cartManager.change(productDetails.id, productDetails.cartQuantity - 1)
    }

    fun showOrHideDetailText() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showDetailText = !s.showDetailText
            )
        }
    }

    fun showAllOrHideProperties() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showAllProperties = !s.showAllProperties)
        }
    }

    fun changeFloatingButton(isVisible: Boolean) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                hideFloatingButton = isVisible
            )
        }
    }

    fun hideMultiBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showMultiBottomSheet = false
            )
        }
    }

    fun showMultiBottomSheet() = viewModelScope.launch {
        _state.update { s ->
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
        _state.update { s ->
            s.copy(
                showPresentBottomSheet = true
            )
        }
    }

    fun hidePresentBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showPresentBottomSheet = false
            )
        }
    }

    fun hidePresentBlockBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showPresentBlockBottomSheet = false
            )
        }
    }

    fun showPresentBlockBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showPresentBlockBottomSheet = true
            )
        }
    }

    fun navigateToAboutProduct() = viewModelScope.launch {
        val viewState = _state.value
        val productDetails = stateSnapshot.productDetails

        aboutProductManager.updateInfo(
            tabs = stateSnapshot.tabs,
            characteristicBlockList = productDetails.characteristics,
            documents = productDetails.documents,
            fullDescription = productDetails.detailInfo
        )

        sendEvent(
            ProductDetailsEvents.GoToAboutProduct(
                viewState.productDetails.id,
                viewState.productDetails.prices,
                viewState.buttons.analogButton,
                viewState.productDetails.isAvailable
            )

        )
    }

    fun showAllComments() = viewModelScope.launch {
        val productDetails = stateSnapshot.productDetails
        sendEvent(
            ProductDetailsEvents.GoToProductComments(
                productDetails.id,
                productDetails.name,
                productDetails.detailPicture
            )
        )
    }

    fun navigateToProductAnalogs() = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToProductAnalogs(stateSnapshot.productDetails.id))
    }

    fun navigateToPreOrder() = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToPreOrder(stateSnapshot.productDetails.id))
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoBack)
    }

    fun navigateToSearch(query: String) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToSearchProductList(query))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToProductDetails(product.id))
    }

    fun changeFavorite() = viewModelScope.launch {
        val productDetails = _state.value.productDetails
        likeManager.changeFavorite(productDetails.id, !productDetails.isFavorite)
    }


    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToCategory(categoryItem: BrandCategoryItemUi) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToCategoryProductList(categoryItem.data.id.toLong()))
    }

    fun share() = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.Share(_state.value.productDetails.shareUrlText))
    }

    fun copyArticleNumber() = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.Copy(_state.value.productDetails.articleNumber))
    }

    fun navigateToDetailMedia(media: ProductMediaUi) = viewModelScope.launch {
        val productDetails = _state.value.productDetails
        val mediaList = productDetails.mediaList

        sendEvent(
            ProductDetailsEvents.GoToDetailMedia(media, mediaList)
        )
    }

    fun navigateToBrandProducts(brandItem: BrandCategoryItemUi) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToBrandProducts(brandItem.data.id.toLong()))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.GoToProductAnalogs(product.id))
    }

    fun addProductWithGift(buyButton: BuyButtonUi) = viewModelScope.launch {
        cartManager.add(listOf(buyButton.productId, buyButton.moreProductId))
        _state.update { s ->
            s.copy(
                showPresentBottomSheet = false,
                showPresentBlockBottomSheet = false
            )
        }
    }

    fun navigateToWriteComment() = viewModelScope.launch {
        val productDetails = _state.value.productDetails

        if (accountManager.fetchAccountId() == null) {
            sendEvent(ProductDetailsEvents.GoToProfile)
        } else {
            sendEvent(
                ProductDetailsEvents.GoToWriteComment(
                    productDetails.id,
                    productDetails.detailPicture,
                    productDetails.name,
                    0
                )
            )

        }
    }

    fun saveMultiProductChoice() = viewModelScope.launch {
        val state = _state.value
        val productDetails = state.productDetails
        cartManager.change(productDetails.id, state.multiProductQuantity)
        _state.update { s ->
            s.copy(showMultiBottomSheet = false)
        }
    }

    fun changeMultiProductQuantity(newMultiProductQuantity: Int) = viewModelScope.launch {
        _state.update { s ->
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
        _state.update { s ->
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
        _state.update { s ->
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
        _state.update { s ->
            s.copy(uiState = ProductDetailsUiState.Success)
        }
        userPreferencesRepository.setCanViewAdultProducts(true)
    }

    fun setMediaPage(page: Int) = viewModelScope.launch {
        sendEvent(ProductDetailsEvents.ScrollToMediaPage(page))
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
        ) : ProductDetailsEvents()

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

        class ScrollToMediaPage(val page: Int) : ProductDetailsEvents()
    }


    @Immutable
    data class ProductDetailsState(
        override val items: List<VodovozSectionUi<ProductUi>> = emptyList(),
        val showDetailText: Boolean = false,
        val showAllProperties: Boolean = false,
        val buttonIsLoading: Boolean = false,
        val hideFloatingButton: Boolean = true,
        val productDetails: ProductDetailsUi = ProductDetailsUi.Empty,
        val comments: List<CommentUi> = emptyList(),
        val buttons: ProductDetailsButtonsUi = ProductDetailsButtonsUi.Empty,
        val tabs: List<ProductDetailsTabUi> = emptyList(),
        val uiState: ProductDetailsUiState = ProductDetailsUiState.Loading,
        val showMultiBottomSheet: Boolean = false,
        val showPresentBottomSheet: Boolean = false,
        val showPresentBlockBottomSheet: Boolean = false,
        val presentInfo: PresentInfoUi = PresentInfoUi.Empty,
        val multiProductQuantity: Int = 1,
        val multiProductTotalPrice: Int = productDetails.firstPrice.price.toInt(),
    ) : ItemsState<VodovozSectionUi<ProductUi>, ProductDetailsState>() {

        override fun withItems(newItems: List<VodovozSectionUi<ProductUi>>): ProductDetailsState {
            return copy(items = newItems)
        }

        val totalPrice: Int = calculateProductPrice(
            productDetails.cartQuantity,
            productDetails.prices
        ).toInt()
    }

    @Stable
    sealed class ProductDetailsUiState {
        data object Loading : ProductDetailsUiState()
        data object Success : ProductDetailsUiState()
        data object Error : ProductDetailsUiState()
        data class ForAdults(val forAdultsUi: ForAdultsUi) : ProductDetailsUiState()

        fun isLoading() = this is Loading
    }
}