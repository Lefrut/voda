package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.withUpdatedCartRecursive
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsEvent
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
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
    blockedProductsFlow = emptyFlow(),
    favoritesFlow = emptyFlow(),
    cartFlow = emptyFlow(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val popupWindow: CartPresentPopupWindowUi? =
        savedStateHandle.get<CartPresentPopupWindowUi>("popupWindow")

    private val coupon: String = savedStateHandle.get<String>("coupon").orEmpty()
    private var initialCartQuantities: Map<Long, Int> = emptyMap()
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

        initialCartQuantities = awaitCurrentCart()
        val initialItems = popupWindow.items.withUpdatedCartRecursive(initialCartQuantities)

        updateState { state ->
            with(popupWindow) {
                state.copy(
                    title = present?.title.orEmpty(),
                    purchase = purchase,
                    button = button,
                    items = initialItems,
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

        val selectedProducts = stateSnapshot.currentProduct
        val changedProducts = stateSnapshot.items.filter { product ->
            product.cartQuantity != initialCartQuantities.getOrDefault(product.id, 0)
        }
        val forAdults = selectedProducts?.forAdults

        when {
            forAdults != null -> {
                showForAdultsDialog(
                    forAdults = forAdults,
                    productId = selectedProducts.id,
                    action = PendingAdultAction.SubmitSelection
                )
            }
            selectedProducts != null -> {
                submitSelectedProduct(selectedProducts)
            }
            changedProducts.isNotEmpty() -> {
                applyLocalChanges(changedProducts)
            }
            else -> {
                navigateToOrdering()
            }
        }
    }

    private suspend fun navigateToOrdering() {
        sendEvent(PreOrderProductsEvent.GoToOrdering(coupon))
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
                incrementLocalQuantity(currentProduct.id)
            }

            PendingAdultAction.SubmitSelection -> {
                submitSelectedProduct(currentProduct)
            }

            PendingAdultAction.None -> Unit
        }
    }

    private suspend fun navigateToCart(product: CartPresentItemUi) {
        sendEvent(PreOrderProductsEvent.GoToCart(listOf(product)))
    }

    private suspend fun navigateToCart(products: List<CartPresentItemUi>) {
        sendEvent(PreOrderProductsEvent.GoToCart(products))
    }


    private suspend fun submitSelectedProduct(product: CartPresentItemUi) {
        updateState { state ->
            state.copy(
                button = state.button.copy(loading = true)
            )
        }

        cartManager.change(product.id, 1).join()
        val isAdded = awaitCurrentCart()[product.id] == 1

        if (isAdded) {
            navigateToCart(product)
        } else {
            updateState { state ->
                state.copy(
                    button = state.button.copy(loading = false)
                )
            }
        }
    }

    fun incrementProduct(product: CartPresentItemUi) = viewModelScope.launch {
        val currentProduct = stateSnapshot.items.firstOrNull { item -> item.id == product.id } ?: product
        val forAdults = product.forAdults

        if (currentProduct.cartQuantity <= 0 && forAdults != null) {
            showForAdultsDialog(
                forAdults = forAdults,
                productId = product.id,
                action = PendingAdultAction.LocalIncrement
            )
        } else {
            incrementLocalQuantity(product.id)
        }
    }

    fun decrementProduct(product: CartPresentItemUi) = viewModelScope.launch {
        updateLocalQuantity(
            productId = product.id,
            quantity = (stateSnapshot.items.firstOrNull { item -> item.id == product.id }?.cartQuantity
                ?: product.cartQuantity) - 1
        )
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

        products.forEach { product ->
            cartManager.change(product.id, product.cartQuantity).join()
        }

        val updatedCart = awaitCurrentCart()
        val allApplied = products.all { product ->
            updatedCart.getOrDefault(product.id, 0) == product.cartQuantity
        }

        if (allApplied) {
            val productsInCart = products.filter { product -> product.cartQuantity > 0 }
            if (productsInCart.isNotEmpty()) {
                navigateToCart(productsInCart)
            } else {
                navigateToOrdering()
            }
        } else {
            updateState { state ->
                state.copy(button = state.button.copy(loading = false))
            }
        }
    }

    private fun incrementLocalQuantity(productId: Long) {
        val currentQuantity = stateSnapshot.items.firstOrNull { item -> item.id == productId }?.cartQuantity ?: 0
        updateLocalQuantity(productId, currentQuantity + 1)
    }

    private fun updateLocalQuantity(productId: Long, quantity: Int) {
        updateState { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == productId) {
                        item.copy(cartQuantity = quantity.coerceAtLeast(0))
                    } else {
                        item
                    }
                }
            )
        }
    }

    private suspend fun awaitCurrentCart(): Map<Long, Int> {
        return withTimeoutOrNull(1000L) {
            cartManager.observeCarts().firstOrNull()
        }.orEmpty()
    }

    private enum class PendingAdultAction {
        None,
        LocalIncrement,
        SubmitSelection,
    }
}
