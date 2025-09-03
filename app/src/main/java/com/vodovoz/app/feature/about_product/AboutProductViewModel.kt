package com.vodovoz.app.feature.about_product

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.about_product.AboutProductManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.DocumentUi
import com.vodovoz.app.design_system.model.PriceUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.about_product.model.AboutProductEvent
import com.vodovoz.app.feature.about_product.model.AboutProductState
import com.vodovoz.app.feature.product_details.model.toUi
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutProductViewModel @Inject constructor(
    private val aboutProductManager: AboutProductManager,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
) : ProductsMviViewModel<ProductUi, AboutProductState, AboutProductEvent>(
    state = AboutProductState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    cartFlow = cartManager.observeCarts(),
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

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun listenCartUpdates() =
        cartManager.observeUpdateCartList().filter { update -> update }.mapLatest {
            updatePresentHtml()
        }.collect()


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

    private fun updatePresentHtml() = viewModelScope.launch {
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
        cartManager.change(productId, stateSnapshot.product.cartQuantity + 1)
    }

    fun decrementProductFromCart() = viewModelScope.launch {
        cartManager.change(productId, stateSnapshot.product.cartQuantity - 1)
    }


}