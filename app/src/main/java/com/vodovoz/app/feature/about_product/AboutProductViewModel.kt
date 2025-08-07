package com.vodovoz.app.feature.about_product

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.about_product.AboutProductManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.DocumentUi
import com.vodovoz.app.design_system.model.PriceUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.about_product.model.AboutProductEvent
import com.vodovoz.app.feature.about_product.model.AboutProductState
import com.vodovoz.app.feature.product_details.model.toUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.calculateProductPrice
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class AboutProductViewModel @Inject constructor(
    private val aboutProductManager: AboutProductManager,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<AboutProductState, AboutProductEvent>(AboutProductState()) {

    private val productId: Long =
        savedStateHandle.get<Long>("productId") ?: navigateBack().run { -1L }

    private val productPrices: List<PriceUi> =
        savedStateHandle.get<List<PriceUi>>("prices") ?: navigateBack().run { emptyList() }

    private val analogButton: ColorfulButtonUi? = savedStateHandle["analogButton"]

    private val isAvailable: Boolean = savedStateHandle["isAvailable"] ?: false


    init {
        initByArguments()
        fetchAboutProductInfo()
    }

    private fun initByArguments() = viewModelScope.launch {
        val price = productPrices.firstOrNull()
        price?.let {
            _state.update { s ->
                s.copy(
                    productPrice = price.price.toInt(),
                    productOldPrice = price.oldPrice.toInt(),
                    analogButton = analogButton,
                    productAvailable = isAvailable
                )
            }
        }
    }

    suspend fun listenLoadingsProduct() = state.combine(
        cartManager.blockedProductsState
    ) { _, blockedProducts ->
        blockedProducts
    }.collectLatest { blockedProducts ->
        _state.update { s ->
            s.copy(buttonIsLoading = productId in blockedProducts)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun listenCartUpdates() =
        cartManager.observeUpdateCartList().filter { update -> update }.mapLatest {
            updatePresentHtml()
        }.collect()


    suspend fun listenCart() = state.combine(
        cartManager.observeCarts()
    ) { _, cartMap ->
        cartMap
    }.collectLatest { cartMap ->
        _state.update { s ->

            val cartQuantity = cartMap.getOrDefault(productId, 0)

            s.copy(
                cartQuantity = cartQuantity,
                productTotalPrice = calculateProductPrice(
                    cartQuantity,
                    productPrices
                ).roundToInt()
            )
        }
    }


    private fun fetchAboutProductInfo() = viewModelScope.launch {
        _state.update { s ->
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
            _state.update { s ->
                s.copy(presentHtml = presentInfo.toUi().html)
            }
        }
    }

    fun selectTab(i: Int) = viewModelScope.launch {
        _state.update { s ->
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
        cartManager.change(productId, stateSnapshot.cartQuantity + 1)
    }

    fun decrementProductFromCart() = viewModelScope.launch {
        cartManager.change(productId, stateSnapshot.cartQuantity - 1)
    }


}