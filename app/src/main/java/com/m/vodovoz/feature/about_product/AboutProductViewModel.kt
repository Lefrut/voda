package com.m.vodovoz.feature.about_product

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.about_product.AboutProductManager
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.design_system.model.PriceUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.about_product.api.AboutProductNavKey
import com.m.vodovoz.feature.about_product.model.AboutProductEvent
import com.m.vodovoz.feature.about_product.model.AboutProductState
import com.m.vodovoz.feature.product_details.model.toUi
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
@HiltViewModel(assistedFactory = AboutProductViewModel.Factory::class)
@Stable
class AboutProductViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val aboutProductManager: AboutProductManager,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
    @Assisted private val navKey: AboutProductNavKey?,
) : ProductsMviViewModel<ProductUi, AboutProductState, AboutProductEvent>(
    state = AboutProductState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    cartFlow = cartManager.observeCarts(),
    favoritesFlow = MutableStateFlow(emptyMap()),
    canViewAdultProducts = MutableStateFlow(false)
) {

    private val productId: Long =
        navKey?.productId ?: savedStateHandle.get<Long>("productId") ?: -1L

    private val productPrices: List<PriceUi> =
        navKey?.prices ?: savedStateHandle.get<List<PriceUi>>("prices") ?: emptyList()

    private val analogButton: ColorfulButtonUi? = navKey?.analogButton ?: savedStateHandle["analogButton"]

    private val isAvailable: Boolean = navKey?.isAvailable ?: savedStateHandle["isAvailable"] ?: false


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
        cartManager.observeRefreshCart().filter { update -> update }.mapLatest {
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

    @AssistedFactory
    interface Factory {
        fun create(navKey: AboutProductNavKey?): AboutProductViewModel
    }

}
