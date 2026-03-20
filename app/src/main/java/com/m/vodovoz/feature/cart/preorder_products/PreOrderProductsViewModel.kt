package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.cart.bottles.AllBottlesFlowViewModel.BottlesUiState
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsEvent
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
@Stable
class PreOrderProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cartManager: CartManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<CartPresentItemUi, PreOrderProductsState, PreOrderProductsEvent>(
    state = PreOrderProductsState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = emptyFlow(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val popupWindow: CartPresentPopupWindowUi? =
        savedStateHandle.get<CartPresentPopupWindowUi>("popupWindow")

    private val coupon: String = savedStateHandle.get<String>("coupon").orEmpty()
    private var pendingAdultProductId: Long? = null
    private var pendingAdultAction: PendingAdultAction = PendingAdultAction.None

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
                    purchase = purchase,
                    button = button,
                    items = items,
                    present = present
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        val productsInCart = stateSnapshot.items.filter { product ->
            product.cartQuantity > 0
        }
        sendEvent(PreOrderProductsEvent.BackToCart(productsInCart))
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

        val selectedProduct = stateSnapshot.currentProduct
        val productsInCart = stateSnapshot.items.filter { product ->
            product.cartQuantity > 0
        }
        val forAdults = selectedProduct?.forAdults

        when {
            forAdults != null -> {
                showForAdultsDialog(
                    forAdults = forAdults,
                    productId = selectedProduct.id,
                    action = PendingAdultAction.SubmitSelection
                )
            }

            selectedProduct != null -> {
                submitSelectedProduct(selectedProduct)
            }

            productsInCart.isNotEmpty() -> {
                applyLocalChanges(productsInCart)
            }

            else -> {
                navigateToOrdering(emptyList())
            }
        }
    }

    private suspend fun navigateToOrdering(productsInCart: List<CartPresentItemUi>) {
        sendEvent(PreOrderProductsEvent.GoToOrdering(coupon, productsInCart))
    }

    private fun showForAdultsDialog(
        forAdults: ForAdultsUi,
        productId: Long? = null,
        action: PendingAdultAction = PendingAdultAction.LocalIncrement,
    ) {
        pendingAdultProductId = productId
        pendingAdultAction = action
        updateState { state ->
            state.copy(showForAdultsDialog = true, forAdultsDialog = forAdults)
        }
    }

    fun closeForAdultsDialog() {
        pendingAdultProductId = null
        pendingAdultAction = PendingAdultAction.None
        updateState { state ->
            state.copy(showForAdultsDialog = false)
        }
    }

    fun acceptForAdults() = viewModelScope.launch {
        userPreferencesRepository.setCanViewAdultProducts(true)
        val currentProduct = pendingAdultProductId?.let { productId ->
            stateSnapshot.items.firstOrNull { item -> item.id == productId }
        } ?: stateSnapshot.currentProduct

        val pendingAction = pendingAdultAction
        pendingAdultProductId = null
        pendingAdultAction = PendingAdultAction.None
        updateState { state ->
            state.copy(showForAdultsDialog = false)
        }

        currentProduct ?: return@launch
        when (pendingAction) {
            PendingAdultAction.LocalIncrement -> {
                incrementProduct(currentProduct)
            }

            PendingAdultAction.SubmitSelection -> {
                submitSelectedProduct(currentProduct)
            }

            PendingAdultAction.None -> Unit
        }
    }

    private suspend fun submitSelectedProduct(product: CartPresentItemUi) {
        updateState { state ->
            state.copy(
                button = state.button.copy(loading = true)
            )
        }

        cartManager.change(product.id, 1).join()

        val isAdded = stateSnapshot.items.any {
            it.cartQuantity == 1
        }

        if (isAdded) {
            navigateToOrdering(listOf(product))
        } else {
            updateState { state ->
                state.copy(
                    button = state.button.copy(loading = false)
                )
            }
        }
    }

    fun incrementProduct(product: CartPresentItemUi) = viewModelScope.launch {
        if (product.cartQuantity >= product.maxQuantity) return@launch

        val forAdults = product.forAdults

        if (forAdults != null) {
            showForAdultsDialog(
                forAdults = forAdults,
                productId = product.id,
                action = PendingAdultAction.LocalIncrement
            )
        } else {
            cartManager.change(product.id, product.cartQuantity + 1)
        }
    }

    fun decrementProduct(product: CartPresentItemUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
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

    private suspend fun applyLocalChanges(products: List<CartPresentItemUi>) {
        updateState { state ->
            state.copy(button = state.button.copy(loading = true))
        }

        delay(250L)
        cartManager.blockedProductsFlow.collectLatest { blockedProducts ->
            if (blockedProducts.isEmpty()) {
                val productsInCart = products.filter { product ->
                    product.cartQuantity > 0
                }
                navigateToOrdering(productsInCart)
            }
        }
    }

    private enum class PendingAdultAction {
        None,
        LocalIncrement,
        SubmitSelection,
    }
}
