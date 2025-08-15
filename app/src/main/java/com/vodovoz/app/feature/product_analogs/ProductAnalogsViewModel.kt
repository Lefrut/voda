package com.vodovoz.app.feature.product_analogs

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.domain.general.model.product.toUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.product_analogs.model.ProductAnalogsEvent
import com.vodovoz.app.feature.product_analogs.model.ProductAnalogsState
import com.vodovoz.app.feature.product_analogs.model.ProductAnalogsUiState
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ProductAnalogsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cartManager: CartManager,
    private val favoritesManager: LikeManager,
    userPreferencesRepository: UserPreferencesRepository
) : ProductsMviViewModel<ProductUi, ProductAnalogsState, ProductAnalogsEvent>(
    state = ProductAnalogsState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = favoritesManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {
    private val productId = savedStateHandle.get<Long>("productId") ?: -1


    fun fetchProductAnalogs() =
        vodovozServiceRepository.getProductAnalogs(productId, stateSnapshot.currentSort.toDomain())
            .onStart {
                _state.update { s -> s.copy(uiState = ProductAnalogsUiState.Loading) }
            }.onEach { result ->
                delay(100)
                result.onSuccess { productsSectionModel ->
                    val productsSectionUi = productsSectionModel.toUi()
                    _state.update { s ->
                        s.copy(
                            productsSection = productsSectionUi,
                            currentSort = if (s.currentSort == SortUi.Empty) productsSectionUi.sorting.firstOrNull()
                                ?: SortUi.Empty.copy(name = productsSectionUi.sortingTitle) else s.currentSort,
                            uiState = ProductAnalogsUiState.Success,
                            items = productsSectionUi.products
                        )
                    }
                }.onFailure {
                    _state.update { s ->
                        s.copy(
                            uiState = ProductAnalogsUiState.Error
                        )
                    }
                }
            }.take(1).launchIn(viewModelScope)

    fun showSortOptionsBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showSortOptionsBottomSheet = true
            )
        }
    }

    fun closeSortOptionsBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showSortOptionsBottomSheet = false
            )
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (sort == stateSnapshot.currentSort) return@launch
        _state.update { s ->
            s.copy(
                currentSort = sort,
            )
        }
        fetchProductAnalogs()
        closeSortOptionsBottomSheet()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ProductAnalogsEvent.GoBack)
    }

    fun switchLayout() = viewModelScope.launch {
        _state.update { s ->
            s.copy(isGridView = !s.isGridView)
        }
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductAnalogsEvent.GoToProductDetails(product.id))
    }

    fun changeProductFavorite(product: ProductUi) = viewModelScope.launch {
        favoritesManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductAnalogsEvent.GoToProductAnalogs(product.id))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }


}