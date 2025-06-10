package com.vodovoz.app.feature.cart

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.model.cart.OrderSummaryItemUi
import com.vodovoz.app.domain.general.model.cart.mapToUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.cart.model.CartButtonUi
import com.vodovoz.app.feature.cart.model.CartItemUi
import com.vodovoz.app.feature.cart.model.CartPresentItemUi
import com.vodovoz.app.feature.cart.model.CartPresentPopupWindowUi
import com.vodovoz.app.feature.cart.model.CartPresentUi
import com.vodovoz.app.feature.cart.model.CartPromoButtonUi
import com.vodovoz.app.feature.cart.model.mapToUi
import com.vodovoz.app.feature.cart.model.toUi
import com.vodovoz.app.feature.cart.model.withUpdatedCart
import com.vodovoz.app.feature.cart.model.withUpdatedFavorites
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class CartFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<CartFlowViewModel.CartState, CartFlowViewModel.CartEvents>(CartState()) {

    init {
        viewModelScope.launch { listenCart() }
        viewModelScope.launch { listenCartUpdates() }
    }

    private suspend fun listenCartUpdates() {
        cartManager.observeUpdateCartList().filter { update -> update }.collect {
            refresh()
            cartManager.updateCartListState(false)
        }
    }

    suspend fun listenCart() =
        uiStateListener.map { it.data.cartItems }.combine(cartManager.observeCarts()) { _, cart ->
            cart
        }.collectLatest { cart ->
            uiStateListener.updateData { s ->
                s.copy(cartItems = s.cartItems.withUpdatedCart(cart))
            }
        }


    suspend fun listenFavorites() {
        uiStateListener.map { it.data.cartItems }
            .combine(likeManager.observeLikes()) { _, favorites ->
                favorites
            }.collectLatest { favorites ->
                uiStateListener.updateData { s ->
                    s.copy(cartItems = s.cartItems.withUpdatedFavorites(favorites))
                }
            }
    }

    fun fetchCartDetails() = viewModelScope.launch {
        if (dataState.uiState is CartUiState.Empty || dataState.uiState == CartUiState.Error) {
            uiStateListener.updateData { s -> s.copy(uiState = CartUiState.Loading) }
        }

        val currentCartVersion = cartManager.cartVersion
        val cartDetailsResult = vodovozServiceRepository.getCartDetails(
            dataState.promoCode
        ).singleResult()

        cartDetailsResult.onSuccess { cartDetails ->
            val cartItems = cartDetails.items.mapToUi()
            uiStateListener.updateData { s ->
                s.copy(
                    title = cartDetails.title,
                    countText = cartDetails.countText,
                    cartItems = cartDetails.items.mapToUi(),
                    present = cartDetails.present?.toUi(),
                    bottlesButton = cartDetails.bottlesButton?.toUi(),
                    promotionalCodeButton = cartDetails.promotionalCodeButton?.toUi(),
                    presentButton = cartDetails.presentButton?.toUi(),
                    uiState = CartUiState.Cart,
                    orderSummary = cartDetails.orderSummary.mapToUi()
                )
            }


            if (currentCartVersion >= cartManager.cartVersion) {
                cartManager.syncCart(
                    cartItems.associate { item -> item.productId to item.quantity }
                )
            }

        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> {
                    CartUiState.Empty(
                        placeholder = t.placeholder?.toUi() ?: VodovozPlaceholderUi.Empty
                    )
                }

                else -> {
                    CartUiState.Error
                }
            }
            uiStateListener.updateData { s ->
                s.copy(uiState = uiState)
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = true)
        }
        fetchCartDetails().join()
        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = false)
        }
    }


    fun showClearCartDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showClearCartDialog = true)
        }
    }

    fun clearCart() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(blockCart = true, showClearCartDialog = false)
        }
        val clearCartResult = vodovozServiceRepository.clearCart().singleResult()

        clearCartResult.onSuccess {
            cartManager.clearCart()
        }

        uiStateListener.updateData { s ->
            s.copy(blockCart = false)
        }
    }

    fun navigateToProductDetails(cartItem: CartItemUi) = viewModelScope.launch {
        eventListener.emit(CartEvents.GoToProductDetails(cartItem.productId))
    }

    fun incrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        cartManager.change(cartItem.productId, cartItem.quantity + 1)
    }

    fun decrementCartItem(cartItem: CartItemUi) = viewModelScope.launch {
        cartManager.change(cartItem.productId, cartItem.quantity - 1)
    }

    fun changeFavorite(cartItem: CartItemUi) = viewModelScope.launch {
        likeManager.changeFavorite(cartItem.productId, !cartItem.isFavorite)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        eventListener.emit(CartEvents.GoToCatalog)
    }

    fun closeClearCartDialog() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(showClearCartDialog = false) }
    }

    fun showTrashDialog(cartItem: CartItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showRemoveItemDialog = true,
                currentRemoveItem = cartItem
            )
        }
    }

    fun closeTrashDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showRemoveItemDialog = false)
        }
    }

    fun removeCartItem(currentRemoveItem: CartItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                blockCart = true,
                showRemoveItemDialog = false,
                currentRemoveItem = null
            )
        }

        vodovozServiceRepository.updateProductInCart(currentRemoveItem.productId, 0).singleResult()
        fetchCartDetails().join()

        uiStateListener.updateData { s ->
            s.copy(
                blockCart = false
            )
        }
    }

    fun showPromotionCodeBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showPromotionCodeBottomSheet = true)
        }
    }

    fun changePromoCode(newValue: String) = viewModelScope.launch {
        uiStateListener.updateData { s ->

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
        uiStateListener.updateData { s ->
            s.copy(showPromotionCodeBottomSheet = false, promoCode = "")
        }
    }

    fun applyPromoCode() = viewModelScope.launch {
        fetchCartDetails().join()

        val correctCoupon = dataState.promotionalCodeButton?.coupon
        if (correctCoupon.isNullOrEmpty()) {
            return@launch
        }

        uiStateListener.updateData { s ->
            s.copy(showPromotionCodeBottomSheet = false, promoCode = correctCoupon)
        }
    }

    fun navigateToGifts() = viewModelScope.launch {
        val userId = accountManager.fetchAccountId()
        if (userId == null) {
            eventListener.emit(CartEvents.GoToProfile)
        } else {
            val present = dataState.present ?: return@launch
            val popupWindow = dataState.present?.popupWindow ?: return@launch
            if (popupWindow.items.isEmpty()) return@launch

            eventListener.emit(CartEvents.GoToGifts(present, popupWindow))
        }
    }

    fun addGiftToCart(presentItem: CartPresentItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(blockCart = true) }

        vodovozServiceRepository.addProductToCart(presentItem.id, 1).singleResult()
        fetchCartDetails().join()

        uiStateListener.updateData { s -> s.copy(blockCart = false) }

    }

    fun navigateToAllBottles() = viewModelScope.launch {
        eventListener.emit(CartEvents.GoToAllBottles)
    }

    fun navigateToOrder() = viewModelScope.launch {
        eventListener.emit(CartEvents.GoToOrder("", ""))
    }

    @Immutable
    data class CartState(
        val title: String = "",
        val countText: String = "",
        val uiState: CartUiState = CartUiState.Loading,
        val cartItems: List<CartItemUi> = emptyList(),
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
        val promoCode: String = "",
    ) : State {
    }

    @Stable
    sealed interface CartUiState {
        data object Loading : CartUiState
        data object Cart : CartUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : CartUiState
        data object Error : CartUiState
    }


    sealed class CartEvents : Event {

        data class GoToOrder(
            val cart: String,
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