package com.vodovoz.app.feature.favorite

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.product.rating.RatingProductManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.model.ProductsSectionUi
import com.vodovoz.app.domain.general.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.model.CategoryDetailUI
import com.vodovoz.app.ui.model.CategoryUI
import com.vodovoz.app.ui.model.SortTypeUI
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class FavoriteFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<FavoriteFlowViewModel.FavoriteState, FavoriteFlowViewModel.FavoriteEvents>(
    FavoriteState()
) {


    private val pagingProductsListener = PagingDataListener(
        onUpdateItems = { itemSnapshotList ->
            val pagedProducts = itemSnapshotList.mapNotNull { product -> product }

            uiStateListener.updateData { s ->
                s.copy(products = pagedProducts)
            }
        }
    )

    fun notifyPagingProducts(productIndex: Int) = viewModelScope.launch {
        pagingProductsListener[productIndex]
    }


    init {
        viewModelScope.launch {
            listenFavorites()
        }
        listenPagingLoadStates()
    }

    private fun listenPagingLoadStates() = viewModelScope.launch {
        pagingProductsListener.collectLoadState { combinedLoadStates ->
            val currentRefreshState = dataState.productsLoadStates.refresh
            val refreshState = when {
                combinedLoadStates.refresh is LoadState.Loading && dataState.products.isNotEmpty() -> {
                    currentRefreshState
                }

                else -> combinedLoadStates.refresh
            }

            uiStateListener.updateData { s ->
                s.copy(
                    productsLoadStates = combinedLoadStates.copy(
                        refresh = refreshState
                    )
                )
            }
        }
    }

    fun checkFavoritesChanges() = viewModelScope.launch {
        val favoritesMap = likeManager.observeLikes().firstOrNull() ?: return@launch
        val currentFavoritesMap = dataState.products.associate { it.id to it.isFavorite }


        //TODO - check new categories
        val newFavorites = favoritesMap.keys - currentFavoritesMap.keys
        if (newFavorites.isNotEmpty()) {
            selectCategory(dataState.currentCategory)
            return@launch
        }


        val commonProducts = favoritesMap.keys.intersect(currentFavoritesMap.keys)

        commonProducts.forEach { productId ->
            val isFavoriteInCache = favoritesMap[productId] ?: false
            val isFavoriteInCurrent = currentFavoritesMap[productId] ?: false

            if (isFavoriteInCache != isFavoriteInCurrent) {
                fetchFavoriteProducts()
                return@launch
            }
        }
    }

    suspend fun listenCart() =
        uiStateListener
            .map { pagingState -> pagingState.data.products }
            .distinctUntilChanged()
            .combine(cartManager.observeCarts()) { _, cart ->
                cart
            }.collectLatest { cart ->
                uiStateListener.updateData { s ->
                    s.copy(
                        products = s.products.withUpdatedCart(cart)
                    )
                }
            }

    suspend fun listenProductLoadings() =
        uiStateListener.map { it.data.products }.distinctUntilChanged()
            .combine(cartManager.blockedProductsState) { _, blockedProducts ->
                blockedProducts
            }.collectLatest { blockedProducts ->
                uiStateListener.updateData { s ->
                    s.copy(
                        products = s.products.withUpdatedLoading(blockedProducts)
                    )
                }
            }

    private suspend fun listenFavorites() {
        uiStateListener.map { pagingState -> pagingState.data.products }
            .combine(likeManager.observeLikes()) { products, favorites ->
                products to favorites
            }.collectLatest { (products, favorites) ->
                uiStateListener.updateData { s ->
                    s.copy(products = products.withUpdatedFavorites(favorites))
                }
            }
    }

    fun fetchFavoriteProducts() = viewModelScope.launch {
        if (dataState.uiState != FavoriteUiState.Success) {
            uiStateListener.updateData { s ->
                s.copy(uiState = FavoriteUiState.Loading)
            }
        }

        val favoriteProductsResult =
            vodovozServiceRepository.getFavoriteProducts(
                productsIds = likeManager.fetchLikeLocalStr() ?: ""
            ).singleResult()
                .map { productsSectionModel -> productsSectionModel.toUi() }


        favoriteProductsResult.onSuccess { productsSectionUi ->
            uiStateListener.updateData { s ->
                val currentSort = s.currentSort.takeIf { value ->
                    value != SortUi.Empty
                } ?: productsSectionUi.sorting.firstOrNull() ?: SortUi.Empty

                s.copy(
                    productsSection = productsSectionUi,
                    currentSort = currentSort,
                    uiState = FavoriteUiState.Success,
                    products = emptyList(),
                    showRefreshIndicator = false
                )
            }

            val currentCategory = dataState.currentCategory.takeIf {
                it == CategoryUi.Empty || dataState.productsSection.categories.contains(it)
            } ?: CategoryUi.Empty

            vodovozServiceRepository.getFavoriteProductsPaged(
                categoryId = currentCategory.id,
                sort = dataState.currentSort.toDomain(),
                productsIds = likeManager.fetchLikeLocalStr() ?: ""
            ).map { pagingData ->
                pagingData.map { productModel -> productModel.toUi() }
            }.collectLatest { pagingData ->
                pagingProductsListener.collectPagingData(
                    pagingData
                )
            }


        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> t.placeholder?.run {
                    FavoriteUiState.Empty(placeholder = toUi())
                } ?: FavoriteUiState.Error

                else -> FavoriteUiState.Error
            }

            uiStateListener.updateData { s ->
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
        if (dataState.uiState !is FavoriteUiState.Loading) {
            uiStateListener.updateData { s ->
                s.copy(showRefreshIndicator = true)
            }
            fetchFavoriteProducts()
        }
    }

    fun navigateToCategories() = viewModelScope.launch {
        eventListener.emit(
            FavoriteEvents.GoToCategories(
                dataState.productsSection.categories,
                dataState.currentCategory
            )
        )
    }

    fun showSortBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(showSortBottomSheet = true) }
    }

    fun hideSortBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(showSortBottomSheet = false) }
    }


    fun switchLayout() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(isGridView = !s.isGridView) }
    }

    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        val newCategory = if (category == dataState.currentCategory) CategoryUi.Empty
        else category

        uiStateListener.updateData { s ->
            s.copy(
                currentCategory = newCategory,
                productsLoadStates = s.productsLoadStates.copy(
                    refresh = LoadState.Loading,
                ),

                )
        }

        eventListener.emit(FavoriteEvents.ScrollToTop)
        fetchFavoriteProducts()
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (sort == dataState.currentSort) return@launch
        uiStateListener.updateData { s -> s.copy(currentSort = sort, showSortBottomSheet = false) }
        fetchFavoriteProducts()
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(FavoriteEvents.GoToProductDetails(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToSearch() = viewModelScope.launch {
        eventListener.emit(FavoriteEvents.GoToSearch)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        eventListener.emit(FavoriteEvents.GoToCatalog)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(FavoriteEvents.GoToProductAnalogs(product.id))
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
        val favoriteCategory: CategoryUI? = null,
        val bestForYouCategoryDetailUI: CategoryDetailUI? = null,
        val availableTitle: String? = null,
        val notAvailableTitle: String? = null,
        val sortType: SortTypeUI = SortTypeUI(),
        val isAvailable: Boolean = true,
        val selectedCategoryId: Long = -1,
        val isFirstLoadSorted: Boolean = false,
        val itemsList: List<Item> = emptyList(),
        val layoutManager: String = LINEAR,
        val emptyTitle: String? = null,
        val emptyMessage: String? = null,
        val scrollToTop: Boolean = false,

        val productsSection: ProductsSectionUi = ProductsSectionUi.Empty,
        val currentSort: SortUi = SortUi.Empty,
        val currentCategory: CategoryUi = CategoryUi.Empty,
        val isGridView: Boolean = true,
        val showSortBottomSheet: Boolean = false,
        val showRefreshIndicator: Boolean = false,
        val uiState: FavoriteUiState = FavoriteUiState.Loading,
        val products: List<ProductUi> = emptyList(),
        val productsLoadStates: CombinedLoadStates = emptyCombinedLoadStates,
    ) : State

    @Immutable
    sealed interface FavoriteUiState {

        data object Loading : FavoriteUiState
        data object Success : FavoriteUiState
        data class Empty(
            val placeholder: VodovozPlaceholderUi,
        ) : FavoriteUiState

        data object Error : FavoriteUiState

    }

    companion object {
        const val LINEAR = "linear"
        const val GRID = "grid"
    }
}