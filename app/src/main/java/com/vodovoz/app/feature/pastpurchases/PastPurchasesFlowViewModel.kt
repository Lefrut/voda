package com.vodovoz.app.feature.pastpurchases

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.itemadapter.bottomitem.BottomProgressItem
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.product.rating.RatingProductManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.favorite.mapper.FavoritesMapper
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.home.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.mapToUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.mapper.PastPurchasesHeaderBundleMapper.mapToUI
import com.vodovoz.app.mapper.ProductMapper.mapToUI
import com.vodovoz.app.ui.model.CategoryUI
import com.vodovoz.app.ui.model.ProductUI
import com.vodovoz.app.ui.model.SortTypeUI
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class PastPurchasesFlowViewModel @Inject constructor(
    private val repository: MainRepository,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val ratingProductManager: RatingProductManager,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<PastPurchasesFlowViewModel.PastPurchasesState, PastPurchasesFlowViewModel.PastPurchasesEvents>(
    PastPurchasesState()
) {

    private val changeLayoutManager = MutableStateFlow(LINEAR)
    fun observeChangeLayoutManager() = changeLayoutManager.asStateFlow()

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
                combinedLoadStates.refresh is LoadState.Loading && dataState.products.isNotEmpty() -> {
                    dataState.productsLoadStates.refresh
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

    fun firstLoad() {
        if (!state.isFirstLoad) {
            uiStateListener.value = state.copy(isFirstLoad = true, loadingPage = true)
            fetchPastPurchasesHeader()
        }
    }

    fun refresh() {
        uiStateListener.value = state.copy(loadingPage = true)
        fetchPastPurchasesHeader()
    }

    private fun fetchPastPurchasesHeader() {
        val userId = accountManager.fetchAccountId()

        viewModelScope.launch {
            flow {
                emit(
                    repository.fetchPastPurchasesHeader(
                        userId = userId,
                        isAvailable = state.data.isAvailable
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                favoriteCategory = checkSelectedFilter(data.favoriteCategoryUI),
                                availableTitle = data.availableTitle,
                                notAvailableTitle = data.notAvailableTitle,
                                emptyTitle = response.data.emptyTitle,
                                emptySubtitle = response.data.emptySubtitle,
                                sortType = data.favoriteCategoryUI?.sortTypeList?.sortTypeList?.firstOrNull { it.value == "default" }
                                    ?: SortTypeUI(sortName = "По популярности"),
                                scrollToTop = true,
                            ),
                            loadingPage = false,
                            error = null
                        )
                    } else {
                        uiStateListener.value =
                            state.copy(loadingPage = false, error = ErrorState.Error())
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "fetch past purchases error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun firstLoadSorted() {
        if (!state.data.isFirstLoadSorted) {
            uiStateListener.value =
                state.copy(data = state.data.copy(isFirstLoadSorted = true), loadingPage = true)
            fetchPastPurchasesSorted()
        }
    }

    fun refreshSorted() {
        uiStateListener.value = state.copy(
            loadingPage = true,
            page = 1,
            loadMore = false,
            bottomItem = null,
            data = state.data.copy(selectedCategoryId = -1)
        )
        fetchPastPurchasesHeader()
        fetchPastPurchasesSorted()
    }

    fun loadMoreSorted() {
        if (state.bottomItem == null && state.page != null) {
            uiStateListener.value = state.copy(loadMore = true, bottomItem = BottomProgressItem())
            fetchPastPurchasesSorted()
        }
    }

    fun changeLayoutManager() {
        val manager = if (state.data.layoutManager == LINEAR) GRID else LINEAR
        uiStateListener.value = state.copy(
            data = state.data.copy(
                layoutManager = manager, itemsList = FavoritesMapper.mapFavoritesListByManager(
                    manager,
                    state.data.itemsList.filterIsInstance<ProductUI>()
                )
            )
        )
        changeLayoutManager.value = manager
    }

    private fun fetchPastPurchasesSorted() {
        val userId = accountManager.fetchAccountId()

        viewModelScope.launch {
            flow {
                emit(
                    repository.fetchPastPurchasesProducts(
                        userId = userId,
                        categoryId = when (state.data.selectedCategoryId) {
                            -1L -> null
                            else -> state.data.selectedCategoryId
                        },
                        sort = state.data.sortType.value,
                        orientation = state.data.sortType.orientation,
                        isAvailable = state.data.isAvailable,
                        page = state.page
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        val mappedFeed = FavoritesMapper.mapFavoritesListByManager(
                            state.data.layoutManager,
                            data
                        )

                        uiStateListener.value = if (data.isEmpty() && !state.loadMore) {
                            state.copy(
                                error = ErrorState.Empty(),
                                loadingPage = false,
                                loadMore = false,
                                bottomItem = null,
                                page = 1
                            )
                        } else {

                            val itemsList = if (state.loadMore) {
                                state.data.itemsList + mappedFeed
                            } else {
                                mappedFeed
                            }

                            state.copy(
                                page = if (mappedFeed.isEmpty()) null else state.page?.plus(1),
                                loadingPage = false,
                                data = state.data.copy(
                                    itemsList = itemsList,
                                    scrollToTop = state.page == 1
                                ),
                                error = null,
                                loadMore = false,
                                bottomItem = null
                            )
                        }

                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error(),
                                page = 1,
                                loadMore = false
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "fetch past purchases sorted error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    private fun checkSelectedFilter(categoryUI: CategoryUI?): CategoryUI? {
        if (categoryUI == null) return null

        if (categoryUI.categoryUIList.isNotEmpty()) {
            categoryUI.categoryUIList = categoryUI.categoryUIList.toMutableList().apply {
                add(
                    0, CategoryUI(
                        id = -1,
                        name = "Все",
                        isSelected = true
                    )
                )
            }
        }
        return categoryUI
    }

    fun isLoginAlready() = accountManager.isAlreadyLogin()

    fun changeCart(productId: Long, quantity: Int, oldQuan: Int) {
        viewModelScope.launch {
            cartManager.add(id = productId, oldCount = oldQuan, newCount = quantity)
        }
    }

    fun changeFavoriteStatus(productId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            likeManager.like(productId, !isFavorite)
        }
    }

    fun changeRating(productId: Long, rating: Float, oldRating: Float) {
        viewModelScope.launch {
            ratingProductManager.rate(productId, rating = rating, oldRating = oldRating)
        }
    }

    fun updateByIsAvailable(bool: Boolean) {
        if (state.data.isAvailable == bool) return
        uiStateListener.value = state.copy(
            data = state.data.copy(isAvailable = bool),
            page = 1,
            loadMore = false,
            loadingPage = true
        )
        fetchPastPurchasesHeader()
        fetchPastPurchasesSorted()
    }

    fun onTabClick(id: Long) {
        val categoryUI = state.data.favoriteCategory ?: return

        uiStateListener.value = state.copy(
            data = state.data.copy(
                favoriteCategory = categoryUI.copy(
                    categoryUIList = categoryUI.categoryUIList.map { it.copy(isSelected = it.id == id) }
                ),
                selectedCategoryId = id,
                scrollToTop = true,
            ),
            page = 1,
            loadMore = false
        )
        fetchPastPurchasesSorted()
    }

//    fun updateByCategory(categoryId: Long?) {
//        if (state.data.selectedCategoryId == categoryId) return
//        uiStateListener.value = state.copy(
//            data = state.data.copy(
//                selectedCategoryId = categoryId ?: -1,
//                sortType = SortTypeUI(),
//            ),
//            page = 1,
//            loadMore = false,
//            loadingPage = true
//        )
//        fetchPastPurchasesSorted()
//    }

    fun updateBySortType(sortType: SortTypeUI) {
        if (state.data.sortType == sortType) return
        val categoryUI = state.data.favoriteCategory
        uiStateListener.value = state.copy(
            data = state.data.copy(
                sortType = sortType,
                selectedCategoryId = -1,
                favoriteCategory = categoryUI?.copy(
                    categoryUIList = categoryUI.categoryUIList.map { it.copy(isSelected = it.id == -1L) }
                ),
                scrollToTop = true,
            ),
            page = 1,
            loadMore = false,
            loadingPage = true
        )
        fetchPastPurchasesSorted()
    }

    fun onPreOrderClick(id: Long, name: String, detailPicture: String) {
        viewModelScope.launch {
            val accountId = accountManager.fetchAccountId()
            if (accountId == null) {
                //eventListener.emit(PastPurchasesEvents.GoToProfile)
                eventListener.emit(PastPurchasesEvents.GoToPreOrder(id, name, detailPicture))
            } else {
                eventListener.emit(PastPurchasesEvents.GoToPreOrder(id, name, detailPicture))
            }
        }
    }

    fun clearScrollToTop() {
        uiStateListener.value = state.copy(data = state.data.copy(scrollToTop = false))
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
        if (dataState.currentSort == sort) return@launch

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
        if (dataState.uiState !is PastPurchasesUiState.Success) uiStateListener.updateData { s ->
            s.copy(uiState = PastPurchasesUiState.Loading)
        }

        val currentSort = dataState.currentSort.toDomain()
        val currentCategoryId = dataState.currentCategory.id

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
                    sort = dataState.currentSort.toDomain(),
                    categoryId = dataState.currentCategory.id
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

            if (dataState.uiState !is PastPurchasesUiState.Success) {
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
        val favoriteCategory: CategoryUI? = null,
        val availableTitle: String? = null,
        val notAvailableTitle: String? = null,
        val sortType: SortTypeUI = SortTypeUI(),
        val isAvailable: Boolean = true,
        val selectedCategoryId: Long = -1,
        val isFirstLoadSorted: Boolean = false,
        val itemsList: List<Item> = emptyList(),
        val layoutManager: String = LINEAR,
        val emptyTitle: String = "",
        val emptySubtitle: String = "",
        val scrollToTop: Boolean = false,

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

    sealed interface PastPurchasesUiState {
        data object Loading : PastPurchasesUiState
        data object Success : PastPurchasesUiState
        data object Error : PastPurchasesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : PastPurchasesUiState
    }

    companion object {
        const val LINEAR = "linear"
        const val GRID = "grid"
    }
}