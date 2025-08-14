package com.vodovoz.app.feature.promotion_details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.PromotionDetailsUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.PagingProductsMviViewModel
import com.vodovoz.app.ui.paging.PagingState
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromotionDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingProductsMviViewModel<PromotionDetailsViewModel.PromotionDetailsState, PromotionDetailsViewModel.PromotionDetailEvent>(
    state = PromotionDetailsState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts()
) {

    private var promotionId = savedState.get<Long>("promotionId")?.toInt() ?: -1

    init {
        fetchPromotionDetails()
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(PromotionDetailEvent.GoToProductAnalogs(product.id))
    }


    fun fetchPromotionDetails() {
        _state.update { s ->
            s.copy(uiState = UiState.Loading)
        }
        vodovozServiceRepository.getPromotionDetails(promotionId)
            .onEach { promotionDetailsResult ->
                promotionDetailsResult.onSuccess { titleAndPromotionDetails ->


                    _state.update { s ->
                        s.copy(
                            promotionDetails = titleAndPromotionDetails.second.toUi(),
                            productsTitle = titleAndPromotionDetails.first.title,
                            uiState = UiState.Success
                        )
                    }

                    vodovozServiceRepository.getPromotionDetailsProductsPaged(promotionId)
                        .onEach { pagingData ->
                            val pg = pagingData.map { productModel ->
                                productModel.toUi()
                            }
                            collectPagingData(pg)
                        }.launchIn(viewModelScope)

                }.onFailure {
                    _state.update { s ->
                        s.copy(uiState = UiState.Error)
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PromotionDetailEvent.GoBack)
    }

    fun navigateToWebView(url: String) = viewModelScope.launch {
        sendEvent(PromotionDetailEvent.OpenUrl(url))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(PromotionDetailEvent.GoToProductDetails(product.id))
    }

    fun changeProductFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    @Immutable
    data class PromotionDetailsState(
        val promotionDetails: PromotionDetailsUi = PromotionDetailsUi.Empty,
        val productsTitle: String = "",
        override val items: List<ProductUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val uiState: UiState = UiState.Loading,
    ) : PagingState<ProductUi, PromotionDetailsState>() {
        override fun copyPagingState(
            items: List<ProductUi>,
            loadStates: CombinedLoadStates,
        ): PromotionDetailsState = copy(items = items, loadStates = loadStates)
    }

    @Stable
    sealed interface UiState {
        data object Loading : UiState
        data object Error : UiState
        data object Success : UiState
    }

    sealed class PromotionDetailEvent : Event {
        data class OpenUrl(val url: String) : PromotionDetailEvent()
        data class GoToProductAnalogs(val productId: Long) : PromotionDetailEvent()
        data class GoToProductDetails(val productId: Long) : PromotionDetailEvent()
        data object GoBack : PromotionDetailEvent()
    }
}