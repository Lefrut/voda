package com.vodovoz.app.feature.favorite

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.exceptions.EmptyResultException
import com.vodovoz.app.domain.general.model.product.ProductsSectionUi
import com.vodovoz.app.domain.general.model.product.toUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.PagingProductsMviViewModel
import com.vodovoz.app.ui.paging.PagingState
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class FavoriteFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : PagingProductsMviViewModel<ProductUi, FavoriteFlowViewModel.FavoriteState, FavoriteFlowViewModel.FavoriteEvents>(
    state = FavoriteState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {
    private fun hasFavoriteChanges(): Boolean {
        val oldLikes = stateSnapshot.lastSavedLikes

        val newLikes = likeManager.getLikes()
        val newLikeCategories = likeManager.getLikesCategories()
        val selectedCategory = likeManager.selectedCategoryId

        val likesIds = (newLikes.keys + oldLikes.keys).toSet()

        _state.update { s ->
            s.copy(lastSavedLikes = newLikes)
        }

        likesIds.forEach { id ->
            val newLikeCategory = newLikeCategories[id]
            val newLike = newLikes[id]
            val oldLike = oldLikes[id]

            if ((newLike != oldLike || newLike == null) && (newLikeCategory == selectedCategory || selectedCategory == null || newLikeCategory == null)) {
                return true
            }
        }

        return false
    }

    fun fetchFavoritesIfChanges() = viewModelScope.launch {
        if (hasFavoriteChanges()) {
            fetchFavoriteProducts().join()
        }
    }

    fun fetchFavoriteProducts() = viewModelScope.launch {
        if (stateSnapshot.uiState != FavoriteUiState.Success) {
            _state.update { s ->
                s.copy(uiState = FavoriteUiState.Loading)
            }
        }

        val favoriteProductsResult =
            vodovozServiceRepository.getFavoriteProducts(
                productsIds = likeManager.fetchLocalFavorites()
            ).singleResult().map { productsSectionModel ->
                productsSectionModel.toUi()
            }


        favoriteProductsResult.onSuccess { productsSectionUi ->
            _state.update { s ->
                val currentSort = s.currentSort.takeIf { value ->
                    value != SortUi.Empty
                } ?: productsSectionUi.sorting.firstOrNull() ?: SortUi.Empty

                s.copy(
                    productsSection = productsSectionUi,
                    currentSort = currentSort,
                    uiState = FavoriteUiState.Success,
                    items = emptyList(),
                    showRefreshIndicator = false,
                )
            }

            val currentCategory = stateSnapshot.currentCategory.takeIf {
                it == CategoryUi.Empty || stateSnapshot.productsSection.categories.contains(it)
            } ?: CategoryUi.Empty

            viewModelScope.launch {
                vodovozServiceRepository.getFavoriteProductsPaged(
                    categoryId = currentCategory.id,
                    sort = stateSnapshot.currentSort.toDomain(),
                    productsIds = likeManager.fetchLocalFavorites()
                ).map { pagingData ->
                    pagingData.map { productModel -> productModel.toUi() }
                }.collectPagingData()
            }

        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> t.placeholder?.run {
                    FavoriteUiState.Empty(placeholder = toUi())
                } ?: FavoriteUiState.Error

                else -> FavoriteUiState.Error
            }

            _state.update { s ->
                val productsSection = s.productsSection
                val title =
                    (uiState as? FavoriteUiState.Empty)?.placeholder?.title ?: productsSection.title
                s.copy(
                    productsSection = productsSection.copy(title = title),
                    uiState = uiState,
                    showRefreshIndicator = false
                )
            }
        }

    }

    fun refresh() = viewModelScope.launch {
        if (stateSnapshot.uiState !is FavoriteUiState.Loading) {
            _state.update { s ->
                s.copy(showRefreshIndicator = true)
            }
            fetchFavoriteProducts()
        }
    }

    fun navigateToCategories() = viewModelScope.launch {
        sendEvent(
            FavoriteEvents.GoToCategories(
                stateSnapshot.productsSection.categories,
                stateSnapshot.currentCategory
            )
        )
    }

    fun showSortBottomSheet() = viewModelScope.launch {
        _state.update { s -> s.copy(showSortBottomSheet = true) }
    }

    fun hideSortBottomSheet() = viewModelScope.launch {
        _state.update { s -> s.copy(showSortBottomSheet = false) }
    }


    fun switchLayout() = viewModelScope.launch {
        _state.update { s -> s.copy(isGridView = !s.isGridView) }
    }

    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        val newCategory = if (category == stateSnapshot.currentCategory) CategoryUi.Empty
        else category

        _state.update { s ->
            s.copy(
                currentCategory = newCategory,
                loadStates = s.loadStates.copy(refresh = LoadState.Loading)
            )
        }

        sendEvent(FavoriteEvents.ScrollToTop)
        likeManager.changeCategory(category.id.takeIf { it != CategoryUi.Empty.id })
        fetchFavoriteProducts().join()
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (sort == stateSnapshot.currentSort) return@launch
        _state.update { s -> s.copy(currentSort = sort, showSortBottomSheet = false) }
        fetchFavoriteProducts().join()
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(FavoriteEvents.GoToProductDetails(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(FavoriteEvents.GoToSearch)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(FavoriteEvents.GoToCatalog)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(FavoriteEvents.GoToProductAnalogs(product.id))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }


    sealed class FavoriteEvents : Event {
        data object GoToProfile : FavoriteEvents()
        data object ScrollToTop : FavoriteEvents()
        data object GoToSearch : FavoriteEvents()
        data object GoToCatalog : FavoriteEvents()

        data class GoToCategories(
            val categories: List<CategoryUi>,
            val category: CategoryUi,
        ) : FavoriteEvents()

        data class GoToProductDetails(val productId: Long) : FavoriteEvents()
        data class GoToProductAnalogs(val productId: Long) : FavoriteEvents() {

        }
    }

    @Immutable
    data class FavoriteState(
        val productsSection: ProductsSectionUi = ProductsSectionUi.Empty,
        val currentSort: SortUi = SortUi.Empty,
        val currentCategory: CategoryUi = CategoryUi.Empty,
        val isGridView: Boolean = true,
        val showSortBottomSheet: Boolean = false,
        val showRefreshIndicator: Boolean = false,
        val uiState: FavoriteUiState = FavoriteUiState.Loading,
        override val items: List<ProductUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val lastSavedLikes: Map<Long, Boolean> = emptyMap(),
    ) : PagingState<ProductUi, FavoriteState>() {

        override fun copyPagingState(
            items: List<ProductUi>,
            loadStates: CombinedLoadStates,
        ): FavoriteState = copy(items = items, loadStates = loadStates)
    }

    @Stable
    sealed interface FavoriteUiState {

        data object Loading : FavoriteUiState
        data object Success : FavoriteUiState
        data class Empty(
            val placeholder: VodovozPlaceholderUi,
        ) : FavoriteUiState

        data object Error : FavoriteUiState

    }
}