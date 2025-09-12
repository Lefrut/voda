package com.vodovoz.app.feature.cart

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.order.OrderSummaryItemUi
import com.vodovoz.app.design_system.model.order.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.exceptions.EmptyResultException
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.cart.model.CartButtonUi
import com.vodovoz.app.feature.cart.model.CartItemUi
import com.vodovoz.app.feature.cart.model.CartPresentItemUi
import com.vodovoz.app.feature.cart.model.CartPresentPopupWindowUi
import com.vodovoz.app.feature.cart.model.CartPresentUi
import com.vodovoz.app.feature.cart.model.CartPromoButtonUi
import com.vodovoz.app.feature.cart.model.mapToUi
import com.vodovoz.app.feature.cart.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.ItemsState
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class CartFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val tabManager: TabManager,
    userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<CartItemUi, CartFlowViewModel.CartState, CartFlowViewModel.CartEvents>(
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
            tabManager.updateBottomNavCartState()
            cartManager.updateRefreshCart(false)
        }
    }

    fun fetchCartDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState is CartUiState.Empty || stateSnapshot.uiState == CartUiState.Error) {
            updateState { s -> s.copy(uiState = CartUiState.Loading) }
        }

        val currentCartVersion = cartManager.cartVersion
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
                    items = cartDetails.items.mapToUi(),
                    present = cartDetails.present?.toUi(),
                    bottlesButton = cartDetails.bottlesButton?.toUi(),
                    promotionalCodeButton = promoButton,
                    presentButton = cartDetails.presentButton?.toUi(),
                    uiState = CartUiState.Cart,
                    orderSummary = cartDetails.orderSummary.mapToUi(),
                    promoCode = promoButton?.popupWindow?.value ?: s.promoCode
                )
            }


            if (currentCartVersion >= cartManager.cartVersion) {

                cartManager.syncCart(
                    cartItems.associate { item -> item.id to item.cartQuantity }
                )

                setSensitiveButtonsAvailability(true)
            }

        }.onFailure { t ->

            val uiState = when (t) {
                is EmptyResultException -> {
                    cartManager.syncCart(emptyMap())
                    CartUiState.Empty(
                        placeholder = t.placeholder?.toUi() ?: VodovozPlaceholderUi.Empty
                    )
                }

                else -> {
                    CartUiState.Error
                }
            }
            updateState { s ->
                s.copy(uiState = uiState)
            }
        }
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


    fun showClearCartDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showClearCartDialog = true)
        }
    }

    fun clearCart() = viewModelScope.launch {
        updateState { s ->
            s.copy(blockCart = true, showClearCartDialog = false)
        }
        val clearCartResult = vodovozServiceRepository.clearCart().singleResult()

        clearCartResult.onSuccess {
            cartManager.clearCart()
        }

        updateState { s ->
            s.copy(blockCart = false)
        }
    }

    fun navigateToProductDetails(cartItem: CartItemUi) = viewModelScope.launch {
        sendEvent(CartEvents.GoToProductDetails(cartItem.id))
    }

    fun incrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        setSensitiveButtonsAvailability(false)
        cartManager.change(cartItem.id, cartItem.cartQuantity + 1)
    }

    fun decrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        setSensitiveButtonsAvailability(false)
        cartManager.change(cartItem.id, cartItem.cartQuantity - 1)
    }

    private fun setSensitiveButtonsAvailability(buttonEnabled: Boolean) {
        updateState { s ->
            s.copy(
                present = s.present?.copy(
                    button = s.present.button?.copy(enabled = buttonEnabled)
                ),
                presentButton = s.presentButton?.copy(enabled = buttonEnabled),
                blockOrderButton = !buttonEnabled
            )
        }

    }

    fun changeFavorite(cartItem: CartItemUi) = viewModelScope.launch {
        likeManager.changeFavorite(cartItem.id, !cartItem.isFavorite)
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
            s.copy(showRemoveItemDialog = false)
        }
    }

    fun removeCartItem(currentRemoveItem: CartItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                blockCart = true,
                showRemoveItemDialog = false,
                currentRemoveItem = null
            )
        }

        vodovozServiceRepository.updateProductInCart(currentRemoveItem.id, 0).singleResult()
        fetchCartDetails().join()

        updateState { s ->
            s.copy(
                blockCart = false
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
                promoCode = newValue,
                promotionalCodeButton = promoButton?.copy(
                    popupWindow = promoButton.popupWindow.copy(
                        errorText = null
                    )
                )
            )
        }
    }

    fun closePromoCodeBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showPromotionCodeBottomSheet = false, promoCode = "")
        }
    }

    fun applyPromoCode() = viewModelScope.launch {
        updateState { s ->
            val promoButton = s.promotionalCodeButton
            s.copy(
                promotionalCodeButton = promoButton?.copy(
                    popupWindow = promoButton.popupWindow.copy(buttonIsLoading = true)
                )
            )
        }
        fetchCartDetails().join()

        val correctCoupon = stateSnapshot.promotionalCodeButton?.coupon
        if (correctCoupon.isNullOrEmpty()) {
            return@launch
        }

        updateState { s ->
            s.copy(
                showPromotionCodeBottomSheet = false,
                promoCode = correctCoupon,
                promotionalCodeButton = s.promotionalCodeButton?.copy(
                    popupWindow = s.promotionalCodeButton.popupWindow.copy(
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
            val popupWindow = stateSnapshot.present?.popupWindow ?: return@launch
            if (popupWindow.items.isEmpty()) return@launch

            sendEvent(CartEvents.GoToGifts(present, popupWindow))
        }
    }

    fun addGiftToCart(presentItem: CartPresentItemUi) = viewModelScope.launch {
        updateState { s -> s.copy(blockCart = true) }

        vodovozServiceRepository.addProductToCart(presentItem.id, 1).singleResult()
        fetchCartDetails().join()

        updateState { s -> s.copy(blockCart = false) }

    }

    fun navigateToAllBottles() = viewModelScope.launch {
        sendEvent(CartEvents.GoToAllBottles)
    }

    fun navigateToOrder() = viewModelScope.launch {
        if (accountManager.fetchAccountId() != null) {
            sendEvent(CartEvents.GoToOrder(""))
        } else {
            sendEvent(CartEvents.GoToProfile)
        }
    }

    @Immutable
    data class CartState(
        val title: String = "",
        val countText: String = "",
        val uiState: CartUiState = CartUiState.Loading,
        override val items: List<CartItemUi> = emptyList(),
        val present: CartPresentUi? = null,
        val bottlesButton: CartButtonUi? = null,
        val promotionalCodeButton: CartPromoButtonUi? = null,
        val presentButton: CartButtonUi? = null,
        val showClearCartDialog: Boolean = false,
        val showRemoveItemDialog: Boolean = false,
        val currentRemoveItem: CartItemUi? = null,
        val showRefreshIndicator: Boolean = false,
        val blockCart: Boolean = false,
        val orderSummary: List<OrderSummaryItemUi> = emptyList(),
        val showPromotionCodeBottomSheet: Boolean = false,
        val blockOrderButton: Boolean = false,
        val promoCode: String = "",
    ) : ItemsState<CartItemUi, CartState>() {

        override fun withItems(newItems: List<CartItemUi>): CartState = copy(items = newItems)
    }

    @Stable
    sealed interface CartUiState {
        data object Loading : CartUiState
        data object Cart : CartUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : CartUiState
        data object Error : CartUiState

        val placeholderOrNull: VodovozPlaceholderUi? get() {
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
        data object GoToAllBottles : CartEvents()

        data class GoToProductDetails(val productId: Long) : CartEvents()
    }
}