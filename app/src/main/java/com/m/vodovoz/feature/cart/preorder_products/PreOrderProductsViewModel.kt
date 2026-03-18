package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsEvent
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PreOrderProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cartManager: CartManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<CartPresentItemUi, PreOrderProductsState, PreOrderProductsEvent>(
    state = PreOrderProductsState(),
    blockedProductsFlow = emptyFlow(),
    favoritesFlow = emptyFlow(),
    cartFlow = emptyFlow(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val popupWindow: CartPresentPopupWindowUi? =
        savedStateHandle.get<CartPresentPopupWindowUi>("popupWindow")

    //todo
    private val coupon: String = savedStateHandle.get<String>("coupon").orEmpty()

    init {
        initializeData()
    }

    private fun initializeData() = viewModelScope.launch {
        if (popupWindow == null) {
            navigateBack()
            return@launch
        }

        updateState { state ->
            with(popupWindow) {
                state.copy(
                    title = present?.title.orEmpty(),
                    description = description,
                    button = button,
                    items = items,
                    present = present
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PreOrderProductsEvent.GoBack)
    }

    fun selectProduct(product: CartPresentItemUi) = viewModelScope.launch {


        updateState { state ->
            val finalProduct = if (product.id == state.currentProduct?.id) {
                null
            } else product

            state.copy(currentProduct = finalProduct)
        }
    }

    fun tryToAddProduct() = viewModelScope.launch {
        val currentProduct = stateSnapshot.currentProduct
        val forAdults = currentProduct?.forAdults

        if (currentProduct == null) {
            navigateToOrdering()
        } else if (forAdults != null) {
            showForAdultsDialog(forAdults)
        } else {
            addSelectedProductToCart(currentProduct)
        }
    }

    private suspend fun navigateToOrdering() {
        sendEvent(PreOrderProductsEvent.GoToOrdering(coupon))
    }

    private fun showForAdultsDialog(forAdults: ForAdultsUi) {
        updateState { state ->
            state.copy(showForAdultsDialog = true, forAdultsDialog = forAdults)
        }
    }

    fun closeForAdultsDialog() {
        updateState { state ->
            state.copy(showForAdultsDialog = false)
        }
    }

    fun acceptForAdults() = viewModelScope.launch {
        userPreferencesRepository.setCanViewAdultProducts(true)
        val currentProduct = stateSnapshot.currentProduct ?: return@launch
        addSelectedProductToCart(currentProduct)
    }

    private suspend fun navigateToCart(productId: Long) {
        sendEvent(PreOrderProductsEvent.GoToCart(productId))
    }

    private suspend fun addSelectedProductToCart(product: CartPresentItemUi) {
        updateState { state ->
            state.copy(
                button = state.button.copy(loading = true)
            )
        }

        cartManager.change(product.id, 1).join()
        val isAdded = combine(
            cartManager.observeCarts(),
            cartManager.blockedProductsFlow
        ) { cart, blockedProducts ->
            if (blockedProducts.contains(product.id)) {
                null
            } else {
                (cart[product.id] ?: 0) > 0
            }
        }.first { added -> added != null } == true

        if (isAdded) {
            navigateToCart(product.id)
        } else {
            updateState { state ->
                state.copy(
                    button = state.button.copy(loading = false)
                )
            }
        }
    }

    fun showPreviewImageDialog(image: String) {
        updateState { state ->
            state.copy(previewImage = image)
        }
    }

    fun closePreviewImageDialog() {
        updateState { state ->
            state.copy(previewImage = null)
        }
    }
}
