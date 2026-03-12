package com.m.vodovoz.feature.cart

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.order.OrderSummaryItemUi
import com.m.vodovoz.design_system.model.order.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.cart.bottles.model.BottleUi
import com.m.vodovoz.feature.cart.bottles.model.toBottle
import com.m.vodovoz.feature.cart.model.AdditionalProductsBSUi
import com.m.vodovoz.feature.cart.model.AdditionalProductsTextUi
import com.m.vodovoz.feature.cart.model.CartButtonUi
import com.m.vodovoz.feature.cart.model.CartItemUi
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.feature.cart.model.CartPromoButtonUi
import com.m.vodovoz.feature.cart.model.mapToUi
import com.m.vodovoz.feature.cart.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingProductsMviViewModel2
import com.m.vodovoz.ui.paging.PagingState2
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class CartFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    val tabManager: TabManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) : PagingProductsMviViewModel2<CartItemUi, ProductUi, CartFlowViewModel.CartState, CartFlowViewModel.CartEvents>(
    state = CartState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {
    init {
        viewModelScope.launch { listenCartUpdates() }
    }

    private suspend fun listenCartUpdates() {
        cartManager.observeRefreshCart().filter { update -> update }.collect {
            refresh()
            cartManager.updateRefreshCart(false)
        }
    }

    fun fetchCartDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState is CartUiState.Empty || stateSnapshot.uiState == CartUiState.Error) {
            updateState { s -> s.copy(uiState = CartUiState.Loading) }
        }

        val currentCartVersion = cartManager.cartVersion

        val updateBottomCartJob = launch { tabManager.updateBottomNavCartState() }
        val cartDetailsResult = vodovozServiceRepository.getCartDetails(
            stateSnapshot.promoCode
        ).singleResult()

        cartDetailsResult.onSuccess { cartDetails ->
            val cartItems = cartDetails.items.mapToUi()
            val promoButton = cartDetails.promotionalCodeButton?.toUi()

            updateState { s ->
                s.copy(
                    title = cartDetails.title,
                    countText = cartDetails.countText,
                    items1 = cartDetails.items.mapToUi(),
                    present = cartDetails.present?.toUi(),
                    bottlesButton = cartDetails.bottlesButton?.toUi(),
                    promotionalCodeButton = promoButton,
                    presentButton = cartDetails.presentButton?.toUi(),
                    uiState = CartUiState.Cart,
                    orderSummary = cartDetails.orderSummary.mapToUi(),
                    promoCode = s.promoCode.ifEmpty { promoButton?.popupWindow?.value.orEmpty() }
                )
            }


            if (currentCartVersion >= cartManager.cartVersion) {

                cartManager.syncCart(
                    cartItems.associate { item -> item.id to item.cartQuantity }
                )

                setSensitiveButtonsAvailability(true)
            }

        }.onFailure { t ->

            val (uiState, state) = when (t) {
                is EmptyResultException -> {
                    cartManager.syncCart(emptyMap())
                    val placeholder = t.placeholder?.toUi() ?: VodovozPlaceholderUi.Empty
                    val items2 = placeholder.productsSection?.items ?: emptyList()

                    CartUiState.Empty(
                        placeholder = placeholder
                    ) to stateSnapshot.copy(items2 = items2)
                }

                else -> {
                    CartUiState.Error to stateSnapshot
                }
            }
            updateState { s ->
                state.copy(uiState = uiState)
            }
        }
        updateBottomCartJob.join()
    }

    fun refresh() = viewModelScope.launch {
        updateState { s ->
            s.copy(showRefreshIndicator = true)
        }
        fetchCartDetails().join()
        updateState { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun closeRecommendationsBS() {
        updateState { s ->
            s.copy(
                showAdditionalProductsBS = false,
                additionalProductsBS = null,
                items2 = emptyList()
            )
        }
    }

    fun fetchAndShowRecommendationsBS(additionalProductsText: AdditionalProductsTextUi) =
        vodovozServiceRepository.getAdditionalProductsBS(
            productsId = additionalProductsText.productsId,
            productsArticle = additionalProductsText.articleNumber
        ).onStart {
            updateState { s ->
                s.copy(showAdditionalProductsBS = true)
            }
        }.map { bsResult ->
            bsResult.mapCatching { bSModel -> bSModel.toUi() }
        }.onEach { bsResult ->
            bsResult.onSuccess {
                updateState { s ->
                    s.copy(additionalProductsBS = bsResult.getOrNull())
                }
                vodovozServiceRepository.getAdditionalProductsPaged(
                    productsId = additionalProductsText.productsId,
                    productsArticle = additionalProductsText.articleNumber
                ).map { data ->
                    data.map {
                        val ui = it.toUi()
                        ui
                    }
                }.collectPagingData2()
            }.onFailure {
                closeRecommendationsBS()
            }
        }.launchIn(viewModelScope)

    fun showClearCartDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showClearCartDialog = true)
        }
    }

    fun clearCart() = viewModelScope.launch {
        updateState { s ->
            s.copy(lockCart = true, showClearCartDialog = false)
        }
        val clearCartResult = vodovozServiceRepository.clearCart().singleResult()

        clearCartResult.onSuccess {
            cartManager.clearCart()
        }

        updateState { s ->
            s.copy(lockCart = false)
        }
    }

    fun navigateToProductDetails(cartItem: CartItemUi) = viewModelScope.launch {
        sendEvent(CartEvents.GoToProductDetails(cartItem.id))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(CartEvents.GoToProductDetails(productId = product.id))
    }

    fun navigateToAnalogsOrShow18(product: ProductUi) = viewModelScope.launch {
        if (product.forAdults != null) {
            updateState { s ->
                s.copy(
                    forAdultsUi = product.forAdults
                )
            }
        } else sendEvent(CartEvents.GoToAnalogs(product.id))

    }

    fun incrementProduct(product: ProductUi) = viewModelScope.launch {
        changeCartQuantity(product.id, product.cartQuantity + 1)
    }

    fun decrementProduct(product: ProductUi) = viewModelScope.launch {
        changeCartQuantity(product.id, product.cartQuantity - 1)
    }


    fun incrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        changeCartQuantity(cartItem.id, cartItem.cartQuantity + 1)
    }

    fun decrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        changeCartQuantity(cartItem.id, cartItem.cartQuantity - 1)
    }

    private suspend fun changeCartQuantity(id: Long, quantity: Int) {
        setSensitiveButtonsAvailability(false)
        cartManager.change(id, quantity)
    }

    private fun setSensitiveButtonsAvailability(buttonEnabled: Boolean) {
        updateState { s ->
            val present = s.present

            s.copy(
                present = present?.copy(
                    button = present.button?.copy(
                        enabled = buttonEnabled
                    )
                ),
                presentButton = s.presentButton?.copy(enabled = buttonEnabled),
                bottlesButton = s.bottlesButton?.copy(
                    enabled = buttonEnabled
                ),
                lockOrderButton = !buttonEnabled
            )
        }

    }

    fun changeFavorite(cartItem: CartItemUi) = viewModelScope.launch {
        likeManager.changeFavorite(cartItem.id, !cartItem.isFavorite)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }


    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(CartEvents.GoToCatalog)
    }

    fun closeClearCartDialog() = viewModelScope.launch {
        updateState { s -> s.copy(showClearCartDialog = false) }
    }

    fun showTrashDialog(cartItem: CartItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showRemoveItemDialog = true,
                currentRemoveItem = cartItem
            )
        }
    }

    fun closeTrashDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showRemoveItemDialog = false,
                currentRemoveItem = null
            )
        }
    }

    fun removeCartItem(currentRemoveItem: CartItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                lockCart = true,
                showRemoveItemDialog = false,
                currentRemoveItem = null
            )
        }

        vodovozServiceRepository.updateProductInCart(currentRemoveItem.id, 0).singleResult()
        fetchCartDetails().join()

        updateState { s ->
            s.copy(
                lockCart = false
            )
        }
    }

    fun showPromotionCodeBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showPromotionCodeBottomSheet = true)
        }
    }

    fun changePromoCode(newValue: String) = viewModelScope.launch {
        updateState { s ->

            val promoButton = s.promotionalCodeButton
            s.copy(
                promotionalCodeButton = promoButton?.copy(
                    popupWindow = promoButton.popupWindow.copy(
                        errorText = null,
                        value = newValue
                    )
                )
            )
        }
    }

    fun closePromoCodeBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showPromotionCodeBottomSheet = false)
        }
    }

    fun applyPromoCode() = viewModelScope.launch {
        updateState { s ->
            val promoButton = s.promotionalCodeButton ?: return@updateState s
            val popupWindow = promoButton.popupWindow
            s.copy(
                promotionalCodeButton = promoButton.copy(
                    popupWindow = popupWindow.copy(buttonIsLoading = true)
                ),
                promoCode = popupWindow.value
            )
        }
        fetchCartDetails().join()

        updateState { s ->
            val button = s.promotionalCodeButton ?: return@updateState s
            val actualCoupon = button.coupon
            s.copy(
                showPromotionCodeBottomSheet = actualCoupon.isBlank(),
                promoCode = actualCoupon,
                promotionalCodeButton = button.copy(
                    popupWindow = button.popupWindow.copy(
                        buttonIsLoading = false
                    )
                )
            )
        }
    }

    fun navigateToGifts() = viewModelScope.launch {
        val userId = accountManager.fetchAccountId()
        if (userId == null) {
            sendEvent(CartEvents.GoToProfile)
        } else {
            val present = stateSnapshot.present ?: return@launch
            val popupWindow = present.popupWindow ?: return@launch
            if (popupWindow.items.isEmpty()) return@launch

            sendEvent(CartEvents.GoToGifts(present, popupWindow))
        }
    }

    fun addGiftToCart(presentItem: CartPresentItemUi) = viewModelScope.launch {
        updateState { s -> s.copy(lockCart = true) }

        vodovozServiceRepository.addProductToCart(presentItem.id, 1).singleResult()
        fetchCartDetails().join()

        updateState { s -> s.copy(lockCart = false) }

    }

    fun navigateToAllBottles() = viewModelScope.launch {
        val bottles = stateSnapshot.items1.filter { item ->
            listOf(
                item.currentPrice,
                item.discountPrice,
                item.basePrice
            ).any { price ->
                price < 0
            }
        }.map { item -> item.toBottle() }


        sendEvent(
            CartEvents.GoToAllBottles(bottles)
        )
    }

    fun navigateToOrder() = viewModelScope.launch {
        if (accountManager.fetchAccountId() != null) {
            sendEvent(CartEvents.GoToOrder(stateSnapshot.promoCode))
        } else {
            sendEvent(CartEvents.GoToProfile)
        }
    }

    fun closeForAdultsPlaceholder() {
        updateState { state -> state.copy(forAdultsUi = null) }
    }

    fun setCanViewForAdults() = viewModelScope.launch {
        userPreferencesRepository.setCanViewAdultProducts(true)
        updateState { s ->
            s.copy(forAdultsUi = null)
        }
    }

    fun cancelPromocode() = viewModelScope.launch {
        changePromoCode("")
        applyPromoCode()
        closePromoCodeBottomSheet()
    }

    @Immutable
    data class CartState(
        val title: String = "",
        val countText: String = "",
        override val items1: List<CartItemUi> = emptyList(),
        override val items2: List<ProductUi> = emptyList(),
        val uiState: CartUiState = CartUiState.Loading,
        val present: CartPresentUi? = null,
        val bottlesButton: CartButtonUi? = null,
        val promotionalCodeButton: CartPromoButtonUi? = null,
        val presentButton: CartButtonUi? = null,
        val showClearCartDialog: Boolean = false,
        val showRemoveItemDialog: Boolean = false,
        val currentRemoveItem: CartItemUi? = null,
        val showRefreshIndicator: Boolean = false,
        val lockCart: Boolean = false,
        val orderSummary: List<OrderSummaryItemUi> = emptyList(),
        val showPromotionCodeBottomSheet: Boolean = false,
        val lockOrderButton: Boolean = false,
        val promoCode: String = "",
        val additionalProductsBS: AdditionalProductsBSUi? = null,
        val showAdditionalProductsBS: Boolean = false,
        override val loadStates1: CombinedLoadStates = emptyCombinedLoadStates,
        override val loadStates2: CombinedLoadStates = emptyCombinedLoadStates,
        val forAdultsUi: ForAdultsUi? = null,
    ) : PagingState2<CartItemUi, ProductUi, CartState>() {


        //todo - mb refactor
        override fun copyPagingState(
            items1: List<CartItemUi>,
            items2: List<ProductUi>,
            loadStates1: CombinedLoadStates,
            loadStates2: CombinedLoadStates,
        ): CartState {
            return copy(
                items1 = items1,
                items2 = items2,
                loadStates1 = loadStates1,
                loadStates2 = loadStates2,
                uiState = if (uiState is CartUiState.Empty) with(uiState) {
                    uiState.copy(
                        placeholder = placeholder.copy(
                            productsSection = placeholder.productsSection?.copy(
                                items = items2
                            )
                        )
                    )
                } else uiState
            )
        }

    }

    @Stable
    sealed interface CartUiState {
        data object Loading : CartUiState
        data object Cart : CartUiState
        data class Empty(
            val placeholder: VodovozPlaceholderUi
        ) : CartUiState

        data object Error : CartUiState

        val placeholderOrNull: VodovozPlaceholderUi?
            get() {
                return (this as? Empty)?.placeholder
            }
    }


    sealed class CartEvents : Event {

        data class GoToOrder(
            val coupon: String,
        ) : CartEvents()

        data class GoToGifts(
            val present: CartPresentUi? = null,
            val popupWindow: CartPresentPopupWindowUi,
        ) : CartEvents()

        data object GoToProfile : CartEvents()
        data object GoToCatalog : CartEvents()
        data class GoToAllBottles(val bottles: List<BottleUi>) : CartEvents()

        data class GoToProductDetails(val productId: Long) : CartEvents()
        data class GoToAnalogs(val productId: Long) : CartEvents()
    }
}