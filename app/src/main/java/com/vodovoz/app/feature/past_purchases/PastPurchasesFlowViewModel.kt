package com.vodovoz.app.feature.past_purchases

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
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.home.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.PagingProductsMviViewModel
import com.vodovoz.app.ui.paging.PagingState
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PastPurchasesFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    userPreferencesRepository: UserPreferencesRepository
) : PagingProductsMviViewModel<ProductUi, PastPurchasesFlowViewModel.PastPurchasesState, PastPurchasesFlowViewModel.PastPurchasesEvents>(
    state = PastPurchasesState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    init {
        viewModelScope.launch { delay(250) }.also {
            fetchPastPurchasesDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(PastPurchasesEvents.GoBack)
    }


    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(PastPurchasesEvents.GoToSearch)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(PastPurchasesEvents.GoToProductAnalogs(product.id))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun hideSortBottomSheet() {
        updateState { s ->
            s.copy(showSortBottomSheet = false)
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        if (stateSnapshot.currentSort == sort) return@launch

        updateState { s ->
            s.copy(
                currentSort = sort,
                showSortBottomSheet = false,
                loadStates = s.loadStates.copy(
                    refresh = LoadState.Loading
                )
            )
        }
        fetchPastPurchasesDetails()
    }

    fun showSortBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showSortBottomSheet = true)
        }
    }

    fun switchLayout() = viewModelScope.launch {
        updateState { s ->
            s.copy(isGridView = !s.isGridView)
        }
    }

    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentCategory = if (category == s.currentCategory) CategoryUi.Empty else category,
                loadStates = s.loadStates.copy(
                    refresh = LoadState.Loading
                )
            )
        }
        fetchPastPurchasesDetails()
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(PastPurchasesEvents.GoToProductDetails(product.id))
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(PastPurchasesEvents.GoToCatalog)
    }

    fun fetchPastPurchasesDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is PastPurchasesUiState.Success) updateState { s ->
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


            updateState { s ->

                val sorting = pastPurchasesDetails.sorting.mapToUi()
                val sort = s.currentSort.takeIf { sort ->
                    sort != SortUi.Empty
                } ?: sorting.firstOrNull() ?: SortUi.Empty

                s.copy(
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
                }.collectPagingData()
            }

        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> t.placeholder?.run {
                    PastPurchasesUiState.Empty(placeholder = toUi())
                } ?: PastPurchasesUiState.Error

                else -> PastPurchasesUiState.Error
            }

            if (stateSnapshot.uiState !is PastPurchasesUiState.Success) {
                updateState { s ->
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
        override val items: List<ProductUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val showSortBottomSheet: Boolean = false,
        val sorting: List<SortUi> = emptyList(),
        val uiState: PastPurchasesUiState = PastPurchasesUiState.Loading,
    ) : PagingState<ProductUi, PastPurchasesState>() {

        override fun copyPagingState(
            items: List<ProductUi>,
            loadStates: CombinedLoadStates,
        ): PastPurchasesState = copy(items = items, loadStates = loadStates)

    }

    @Stable
    sealed interface PastPurchasesUiState {
        data object Loading : PastPurchasesUiState
        data object Success : PastPurchasesUiState
        data object Error : PastPurchasesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : PastPurchasesUiState
    }
}