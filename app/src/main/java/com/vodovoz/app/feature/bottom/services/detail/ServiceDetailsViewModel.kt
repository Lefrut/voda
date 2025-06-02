package com.vodovoz.app.feature.bottom.services.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailsEvent
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailsState
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailsUiState
import com.vodovoz.app.feature.bottom.services.detail.model.toUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ServiceDetailsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cartManager: CartManager,
    private val favoriteManager: LikeManager,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<ServiceDetailsState, ServiceDetailsEvent>(ServiceDetailsState()) {

    private val serviceId: Int = savedStateHandle.get<Int>("serviceId") ?: navigateBack().run {
        -1
    }

    init {
        viewModelScope.launch { delay(250) }.invokeOnCompletion {
            fetchServiceDetails()
        }
    }

    suspend fun listenLoadings() {
        cartManager.blockedProductsState.combine(state) { loadings, _ -> loadings }
            .collectLatest { loadings ->
                _state.update { s ->
                    val productSection = s.productsSection
                    s.copy(
                        productsSection = productSection?.copy(
                            products = productSection.products.withUpdatedLoading(loadings)
                        )
                    )
                }
            }
    }


    suspend fun listenFavorites() {
        favoriteManager.observeLikes().combine(state) { favorites, _ -> favorites }
            .collectLatest { favorites ->
                _state.update { s ->
                    val productSection = s.productsSection
                    s.copy(
                        productsSection = productSection?.copy(
                            products = productSection.products.withUpdatedFavorites(favorites)
                        )
                    )
                }
            }
    }

    suspend fun listenCart() {
        cartManager.observeCarts().combine(state) { cart, _ -> cart }.collectLatest { cart ->
            _state.update { s ->
                val productSection = s.productsSection
                s.copy(
                    productsSection = productSection?.copy(
                        products = productSection.products.withUpdatedCart(cart)
                    )
                )
            }
        }
    }

    fun fetchServiceDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = ServiceDetailsUiState.Loading)
        }

        val serviceDetailsResult = vodovozServiceRepository.getServiceDetails(
            serviceId = serviceId
        ).singleResult()

        serviceDetailsResult.onSuccess { serviceDetails ->

            _state.update { s ->
                s.copy(
                    uiState = ServiceDetailsUiState.Success,
                    title = serviceDetails.name,
                    image = serviceDetails.image,
                    html = serviceDetails.html,
                    button = serviceDetails.button?.toUi(),
                    productsSection = serviceDetails.productsSection?.toUi()
                )
            }

        }.onFailure {
            _state.update { s ->
                s.copy(uiState = ServiceDetailsUiState.Error)
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(ServiceDetailsEvent.GoBack)
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        _events.emit(ServiceDetailsEvent.GoToProductDetails(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        favoriteManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToAnalogs(product: ProductUi) = viewModelScope.launch {
        _events.emit(ServiceDetailsEvent.GoToAnalogs(product.id))
    }

    fun incrementProductToCard(product: ProductUi) = viewModelScope.launch {
        val productSection = stateSnapshot.productsSection ?: return@launch
        val coefficient = productSection.coefficient

        val (first, second) = productSection.additionalProductId.split("-")
            .mapNotNull { it.toLongOrNull() }
        cartManager.add(mapOf(product.id to coefficient, first to second.toInt()))
    }

    fun decrementProductToCard(product: ProductUi) = viewModelScope.launch {
        val coefficient = stateSnapshot.productsSection?.coefficient ?: return@launch

        val newQuantity = product.cartQuantity.run {
            val count = div(coefficient)
            (count - 1).coerceAtLeast(0) * coefficient
        }

        cartManager.change(product.id, newQuantity.coerceAtLeast(0))
    }

    fun navigateToServiceOrder(button: ColorfulButtonUi) = viewModelScope.launch {
        _events.emit(ServiceDetailsEvent.GoToServiceOrder(button.id))
    }

}