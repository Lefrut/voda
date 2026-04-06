package com.m.vodovoz.feature.product_analogs

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.domain.general.model.product.toUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.product_analogs.api.ProductAnalogsNavKey
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsEvent
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsState
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsUiState
import com.m.vodovoz.feature.product_comments.model.SortUi
import com.m.vodovoz.feature.product_comments.model.toDomain
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProductAnalogsViewModel.Factory::class)
@Stable
class ProductAnalogsViewModel @AssistedInject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cartManager: CartManager,
    private val favoritesManager: LikeManager,
    userPreferencesRepository: UserPreferencesRepository,
    @Assisted private val navKey: ProductAnalogsNavKey,
) : ProductsMviViewModel<ProductUi, ProductAnalogsState, ProductAnalogsEvent>(
    state = ProductAnalogsState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = favoritesManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {
    private val productId = navKey.productId


    fun fetchProductAnalogs() =
        vodovozServiceRepository.getProductAnalogs(productId, stateSnapshot.currentSort.toDomain())
            .onStart {
                updateState { s -> s.copy(uiState = ProductAnalogsUiState.Loading) }
            }.onEach { result ->
                delay(100)
                result.onSuccess { productsSectionModel ->
                    val productsSectionUi = productsSectionModel.toUi()
                    updateState { s ->
                        s.copy(
                            productsSection = productsSectionUi,
                            currentSort = if (s.currentSort == SortUi.Empty) productsSectionUi.sorting.firstOrNull()
                                ?: SortUi.Empty.copy(name = productsSectionUi.sortingTitle) else s.currentSort,
                            uiState = ProductAnalogsUiState.Success,
                            items = productsSectionUi.products
                        )
                    }
                }.onFailure {
                    updateState { s ->
                        s.copy(
                            uiState = ProductAnalogsUiState.Error
                        )
                    }
                }
            }.take(1).launchIn(viewModelScope)

    fun showSortOptionsBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showSortOptionsBottomSheet = true
            )
        }
    }

    fun closeSortOptionsBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showSortOptionsBottomSheet = false
            )
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (sort == stateSnapshot.currentSort) return@launch
        updateState { s ->
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
        updateState { s ->
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

    @AssistedFactory
    interface Factory {
        fun create(navKey: ProductAnalogsNavKey): ProductAnalogsViewModel
    }

}
