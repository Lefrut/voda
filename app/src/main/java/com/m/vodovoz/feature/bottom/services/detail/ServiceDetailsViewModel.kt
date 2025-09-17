package com.m.vodovoz.feature.bottom.services.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.bottom.services.detail.model.ServiceDetailsEvent
import com.m.vodovoz.feature.bottom.services.detail.model.ServiceDetailsState
import com.m.vodovoz.feature.bottom.services.detail.model.ServiceDetailsUiState
import com.m.vodovoz.feature.bottom.services.detail.model.ServiceProductsUi
import com.m.vodovoz.feature.bottom.services.detail.model.toUi
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ServiceDetailsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cartManager: CartManager,
    private val favoriteManager: LikeManager,
    userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ProductsMviViewModel<ServiceProductsUi, ServiceDetailsState, ServiceDetailsEvent>(
    state = ServiceDetailsState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = favoriteManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val serviceId: Int = savedStateHandle.get<Int>("serviceId") ?: navigateBack().run { -1 }

    init {
        viewModelScope.launch { delay(250) }.invokeOnCompletion {
            fetchServiceDetails()
        }
    }

    fun fetchServiceDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = ServiceDetailsUiState.Loading)
        }

        val serviceDetailsResult = vodovozServiceRepository.getServiceDetails(
            serviceId = serviceId
        ).singleResult()

        serviceDetailsResult.onSuccess { serviceDetails ->

            updateState { s ->
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
            updateState { s ->
                s.copy(uiState = ServiceDetailsUiState.Error)
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ServiceDetailsEvent.GoBack)
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(ServiceDetailsEvent.GoToProductDetails(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        favoriteManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(ServiceDetailsEvent.GoToAnalogs(product.id))
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
        sendEvent(ServiceDetailsEvent.GoToServiceOrder(button.id))
    }

    fun changeLoading(loading: Boolean) = viewModelScope.launch {
        if (!loading) {
            delay(200)
        }
        updateState { s ->
            s.copy(webViewIsLoading = loading)
        }
    }

}