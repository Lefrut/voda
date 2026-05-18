package com.m.vodovoz.feature.cart.preorder_products

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.cart.change
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.mapToProductUi
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsEvent
import com.m.vodovoz.feature.cart.preorder_products.model.PreOrderProductsState
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PreOrderProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cartManager: CartManager,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<ProductUi, PreOrderProductsState, PreOrderProductsEvent>(
    state = PreOrderProductsState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = emptyFlow(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val popupWindow: CartPresentPopupWindowUi? =
        savedStateHandle.get<CartPresentPopupWindowUi>("popupWindow")

    private val coupon: String = savedStateHandle.get<String>("coupon").orEmpty()
    private var pendingAdultAction: PendingAdultAction = PendingAdultAction.None

    init {
        initializeData()
    }

    private fun initializeData() = viewModelScope.launch {
        if (popupWindow == null) {
            delay(250L)
            navigateBack()
            return@launch
        }

        updateState { state ->
            with(popupWindow) {
                state.copy(
                    title = present?.title.orEmpty(),
                    purchase = purchase,
                    button = button,
                    items = items.map { itemUi ->
                        val label = itemUi.label
                        itemUi.copy(
                            label = label?.copy(
                                backgroundColorValue = label.textColorValue
                            )
                        )
                    }.mapToProductUi()
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PreOrderProductsEvent.BackToCart(currentProductIdsInCart()))
    }

    fun tryToAddProduct() = viewModelScope.launch {
        if (!stateSnapshot.purchase) {
            val selectedProduct = stateSnapshot.items.firstOrNull { item ->
                item.id == stateSnapshot.currentProductId
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

                else -> {
                    navigateToOrdering(emptyList())
                }
            }
            return@launch
        }

        val productIds = currentProductIdsInCart()
        if (productIds.isNotEmpty()) {
            applyLocalChanges()
        } else {
            navigateToOrdering(emptyList())
        }
    }

    fun selectProduct(productId: Long) = viewModelScope.launch {
        updateState { state ->
            state.copy(
                currentProductId = if (state.currentProductId == productId) null else productId
            )
        }
    }

    private suspend fun navigateToOrdering(productIds: List<Long>) {
        sendEvent(PreOrderProductsEvent.GoToOrdering(coupon, productIds))
    }

    private suspend fun submitSelectedProduct(product: ProductUi) {
        updateState { state ->
            state.copy(button = state.button.copy(loading = true))
        }

        cartManager.change(
            product = product,
            count = 1,
        ).join()

        val productIds = currentProductIdsInCart()
        if (productIds.isNotEmpty()) {
            navigateToOrdering(productIds)
        } else {
            updateState { state ->
                state.copy(button = state.button.copy(loading = false))
            }
        }
    }

    private fun showForAdultsDialog(
        forAdults: ForAdultsUi,
        productId: Long,
        action: PendingAdultAction,
    ) {
        pendingAdultAction = action
        updateState { state ->
            state.copy(
                showForAdultsDialog = true,
                forAdultsDialog = forAdults,
                blockedAdultProductId = productId
            )
        }
    }

    fun closeForAdultsDialog() {
        pendingAdultAction = PendingAdultAction.None
        updateState { state ->
            state.copy(showForAdultsDialog = false, blockedAdultProductId = null)
        }
    }

    fun acceptForAdults() = viewModelScope.launch {
        userPreferencesRepository.setCanViewAdultProducts(true)
        val currentProductId = stateSnapshot.blockedAdultProductId
        val action = pendingAdultAction
        pendingAdultAction = PendingAdultAction.None
        updateState { state ->
            state.copy(showForAdultsDialog = false, blockedAdultProductId = null)
        }

        val currentProduct = stateSnapshot.items.firstOrNull { item ->
            item.id == currentProductId
        } ?: return@launch

        when (action) {
            PendingAdultAction.LocalIncrement -> incrementProduct(currentProduct)
            PendingAdultAction.SubmitSelection -> submitSelectedProduct(currentProduct)
            PendingAdultAction.None -> Unit
        }
    }

    fun incrementProduct(product: ProductUi) = viewModelScope.launch {
        if (product.cartQuantity >= product.maxQuantity) return@launch

        val forAdults = product.forAdults

        if (forAdults != null) {
            showForAdultsDialog(
                forAdults = forAdults,
                productId = product.id,
                action = PendingAdultAction.LocalIncrement
            )
        } else {
            cartManager.change(
                product = product,
                count = product.cartQuantity + 1,
            )
        }
    }

    fun decrementProduct(product: ProductUi) = viewModelScope.launch {
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

    private suspend fun applyLocalChanges() {
        updateState { state ->
            state.copy(button = state.button.copy(loading = true))
        }

        delay(250L)
        cartManager.blockedProductsFlow.collectLatest { blockedProducts ->
            if (blockedProducts.isEmpty()) {
                navigateToOrdering(currentProductIdsInCart())
            }
        }
    }

    private fun currentProductIdsInCart(): List<Long> {
        return stateSnapshot.items
            .filter { product -> product.cartQuantity > 0 }
            .map { product -> product.id }
    }

    private enum class PendingAdultAction {
        None,
        LocalIncrement,
        SubmitSelection,
    }
}
