package com.m.vodovoz.feature.promotion_details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.PromotionDetailsUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingProductsMviViewModel
import com.m.vodovoz.ui.paging.PagingState
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import com.m.vodovoz.util.extensions.onEachFailure
import com.m.vodovoz.util.extensions.onEachSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PromotionDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : PagingProductsMviViewModel<ProductUi, PromotionDetailsViewModel.PromotionDetailsState, PromotionDetailsViewModel.PromotionDetailEvent>(
    state = PromotionDetailsState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
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


    fun fetchPromotionDetails(): Job = vodovozServiceRepository.getPromotionDetails(
        promotionId
    ).onStart {
        updateState { s ->
            s.copy(uiState = UiState.Loading)
        }
    }.onEachSuccess { titleAndPromotionDetails ->
        updateState { s ->
            s.copy(
                promotionDetails = titleAndPromotionDetails.second.toUi(),
                productsTitle = titleAndPromotionDetails.first.title,
                uiState = UiState.Success
            )
        }
        vodovozServiceRepository.getPromotionDetailsProductsPaged(
            promotionId = promotionId
        ).map { pagingData ->
            pagingData.map { productModel -> productModel.toUi() }
        }.collectPagingData()
    }.onEachFailure {
        updateState { s ->
            s.copy(uiState = UiState.Error)
        }
    }.launchIn(viewModelScope)


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