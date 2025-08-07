package com.vodovoz.app.feature.past_purchases

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.home.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PastPurchasesFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<PastPurchasesFlowViewModel.PastPurchasesState, PastPurchasesFlowViewModel.PastPurchasesEvents>(
    PastPurchasesState()
) {

    private val pagingProductsListener = PagingDataListener(
        onUpdateItems = { itemSnapshotList ->
            uiStateListener.updateData { s ->
                val pagedProducts = itemSnapshotList.mapNotNull { product -> product }
                s.copy(products = pagedProducts)
            }
        }
    )


    init {
        listenProductsLoadStates()
        viewModelScope.launch { delay(250) }.also {
            fetchPastPurchasesDetails()
        }
    }

    suspend fun listenProductLoadings() = uiStateListener.map { state -> state.data.products }
        .combine(cartManager.blockedProductsState) { _, blockedProducts ->
            blockedProducts
        }.collectLatest { blockedProducts ->
            uiStateListener.updateData { s ->
                s.copy(
                    products = s.products.withUpdatedLoading(blockedProducts)
                )
            }
        }

    suspend fun listenCart() =
        uiStateListener.map { state ->
            state.data.products
        }.combine(cartManager.observeCarts()) { _, cart ->
            cart
        }.collectLatest { cart ->
            uiStateListener.updateData { s ->
                s.copy(
                    products = s.products.withUpdatedCart(cart)
                )
            }
        }

    suspend fun listenFavorites() = uiStateListener.map { pagingState -> pagingState.data.products }
        .combine(likeManager.observeLikes()) { products, favorites ->
            products to favorites
        }.collectLatest { (products, favorites) ->
            uiStateListener.updateData { s ->
                s.copy(
                    products = products.withUpdatedFavorites(favorites)
                )
            }
        }

    private fun listenProductsLoadStates() = viewModelScope.launch {
        pagingProductsListener.collectLoadState { combinedLoadStates ->
            val refreshState = when {
                combinedLoadStates.refresh is LoadState.Loading && stateSnapshot.products.isNotEmpty() -> {
                    stateSnapshot.productsLoadStates.refresh
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

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(PastPurchasesEvents.GoBack)
    }


    fun navigateToSearch() = viewModelScope.launch {
        eventListener.emit(PastPurchasesEvents.GoToSearch)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(PastPurchasesEvents.GoToProductAnalogs(product.id))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun hideSortBottomSheet() {
        uiStateListener.updateData { s ->
            s.copy(showSortBottomSheet = false)
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (stateSnapshot.currentSort == sort) return@launch

        uiStateListener.updateData { s ->
            s.copy(
                currentSort = sort,
                showSortBottomSheet = false,
                productsLoadStates = s.productsLoadStates.copy(
                    refresh = LoadState.Loading
                )
            )
        }
        fetchPastPurchasesDetails()
    }

    fun showSortBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSortBottomSheet = true)
        }
    }

    fun switchLayout() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(isGridView = !s.isGridView)
        }
    }

    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentCategory = if (category == s.currentCategory) CategoryUi.Empty else category,
                productsLoadStates = s.productsLoadStates.copy(
                    refresh = LoadState.Loading
                )
            )
        }
        fetchPastPurchasesDetails()
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        eventListener.emit(PastPurchasesEvents.GoToProductDetails(product.id))
    }

    fun notifyPagingProducts(index: Int) = viewModelScope.launch {
        pagingProductsListener[index]
    }

    fun navigateToCatalog() = viewModelScope.launch {
        eventListener.emit(PastPurchasesEvents.GoToCatalog)
    }

    fun fetchPastPurchasesDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is PastPurchasesUiState.Success) uiStateListener.updateData { s ->
            s.copy(uiState = PastPurchasesUiState.Loading)
        }

        val currentSort = stateSnapshot.currentSort.toDomain()
        val currentCategoryId = stateSnapshot.currentCategory.id

        val pastPurchasesDetailsResult =
            vodovozServiceRepository.getPastPurchasesDetails(
                currentSort,
                currentCategoryId
            ).singleResult()

        pastPurchasesDetailsResult.onSuccess { pastPurchasesDetails ->


            uiStateListener.updateData { s ->

                val sorting = pastPurchasesDetails.sorting.mapToUi()
                val sort = s.currentSort.takeIf { sort ->
                    sort != SortUi.Empty
                } ?: sorting.firstOrNull() ?: SortUi.Empty

                s.copy(
                    products = pastPurchasesDetails.products.mapToUi(),
                    sorting = pastPurchasesDetails.sorting.mapToUi(),
                    categories = pastPurchasesDetails.categories.mapToUi(),
                    title = pastPurchasesDetails.title,
                    currentSort = sort,
                    uiState = PastPurchasesUiState.Success
                )
            }

            viewModelScope.launch {
                vodovozServiceRepository.getPastPurchasesPaged(
                    sort = stateSnapshot.currentSort.toDomain(),
                    categoryId = stateSnapshot.currentCategory.id
                ).map { pagingData ->
                    pagingData.map { productModel -> productModel.toUi() }
                }.collect { pagingData ->
                    pagingProductsListener.collectPagingData(pagingData)
                }
            }

        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> t.placeholder?.run {
                    PastPurchasesUiState.Empty(placeholder = toUi())
                } ?: PastPurchasesUiState.Error

                else -> PastPurchasesUiState.Error
            }

            if (stateSnapshot.uiState !is PastPurchasesUiState.Success) {
                uiStateListener.updateData { s ->
                    val title = (uiState as? PastPurchasesUiState.Empty)?.placeholder?.title
                    s.copy(
                        title = title ?: s.title,
                        uiState = uiState,
                    )
                }
            }
        }
    }

    sealed class PastPurchasesEvents : Event {
        data class GoToPreOrder(val id: Long, val name: String, val detailPicture: String) :
            PastPurchasesEvents()

        data class GoToProductAnalogs(val productId: Long) : PastPurchasesEvents()
        data class GoToProductDetails(val productId: Long) : PastPurchasesEvents()

        data object GoToProfile : PastPurchasesEvents()
        data object GoBack : PastPurchasesEvents()
        data object GoToSearch : PastPurchasesEvents()
        data object GoToCatalog : PastPurchasesEvents()
    }

    @Immutable
    data class PastPurchasesState(
        val title: String = "",
        val categories: List<CategoryUi> = emptyList(),
        val currentCategory: CategoryUi = CategoryUi.Empty,
        val currentSort: SortUi = SortUi.Empty,
        val isGridView: Boolean = true,
        val products: List<ProductUi> = emptyList(),
        val productsLoadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val showSortBottomSheet: Boolean = false,
        val sorting: List<SortUi> = emptyList(),
        val uiState: PastPurchasesUiState = PastPurchasesUiState.Loading,
    ) : State

    @Stable
    sealed interface PastPurchasesUiState {
        data object Loading : PastPurchasesUiState
        data object Success : PastPurchasesUiState
        data object Error : PastPurchasesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : PastPurchasesUiState
    }
}