package com.m.vodovoz.feature.about_product

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.about_product.AboutProductManager
import com.m.vodovoz.common.cart.AppCartManager
import com.m.vodovoz.common.cart.adjustQuantity
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.design_system.model.PriceUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.about_product.model.AboutProductEvent
import com.m.vodovoz.feature.about_product.model.AboutProductState
import com.m.vodovoz.feature.product_details.model.toUi
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutProductViewModel @Inject constructor(
    private val aboutProductManager: AboutProductManager,
    private val cartManager: AppCartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
) : ProductsMviViewModel<ProductUi, AboutProductState, AboutProductEvent>(
    state = AboutProductState(),
    blockedProductsFlow = cartManager.loadingProductIds,
    cartFlow = cartManager.quantities,
    favoritesFlow = MutableStateFlow(emptyMap()),
    canViewAdultProducts = MutableStateFlow(false)
) {

    private val productId: Long =
        savedStateHandle.get<Long>("productId") ?: -1L

    private val productPrices: List<PriceUi> =
        savedStateHandle.get<List<PriceUi>>("prices") ?: emptyList()

    private val analogButton: ColorfulButtonUi? = savedStateHandle["analogButton"]

    private val isAvailable: Boolean = savedStateHandle["isAvailable"] ?: false


    init {
        setupScreen()
        fetchAboutProductInfo()
    }

    private fun setupScreen() = viewModelScope.launch {
        val price = productPrices.firstOrNull()
        updateState { s ->
            s.copy(
                items = price?.let {
                    listOf(
                        ProductUi.Empty.copy(
                            price = price.price,
                            oldPrice = price.oldPrice,
                            id = productId,
                            isAvailable = isAvailable
                        )
                    )
                } ?: emptyList(),
                productPrices = productPrices,
                analogButton = analogButton,
            )
        }
    }

    suspend fun listenCartUpdates() {
        cartManager.results.conflate().collect {
            updatePresentHtml()
        }
    }


    private fun fetchAboutProductInfo() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                tabs = aboutProductManager.tabs,
                characteristics = aboutProductManager.characteristicsBlock,
                description = aboutProductManager.descriptionBlock,
                documents = aboutProductManager.documentsBlock
            )
        }
        updatePresentHtml()
    }

    private suspend fun updatePresentHtml() {
        val result = vodovozServiceRepository.getPresentInfo().singleResult()
        result.onSuccess { presentInfo ->
            updateState { s ->
                s.copy(presentHtml = presentInfo.toUi().html)
            }
        }
    }

    fun selectTab(i: Int) = viewModelScope.launch {
        updateState { s ->
            s.copy(selectedTabIndex = i)
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AboutProductEvent.GoBack)
    }

    fun navigateToDocumentViewer(document: DocumentUi) = viewModelScope.launch {
        sendEvent(AboutProductEvent.GoToDocumentViewer(document))
    }

    fun navigateToProductAnalogs() = viewModelScope.launch {
        sendEvent(AboutProductEvent.GoToProductAnalogs(productId))
    }

    fun incrementProductToCart() = viewModelScope.launch {
        cartManager.adjustQuantity(
            product = stateSnapshot.product,
            delta = 1,
        )
    }

    fun decrementProductFromCart() = viewModelScope.launch {
        cartManager.adjustQuantity(stateSnapshot.product, delta = -1)
    }


}
