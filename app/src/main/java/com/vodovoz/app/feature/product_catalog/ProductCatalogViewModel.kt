package com.vodovoz.app.feature.product_catalog

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import com.vodovoz.app.R
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ForAdultsUi
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.allCategories
import com.vodovoz.app.design_system.model.filters.FiltersPriceUi
import com.vodovoz.app.design_system.model.filters.FiltersUi
import com.vodovoz.app.design_system.model.filters.toDomain
import com.vodovoz.app.design_system.model.findParentOfOnlyLeaf
import com.vodovoz.app.design_system.model.toCategory
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.model.FiltersModel
import com.vodovoz.app.domain.general.model.product.ProductModel
import com.vodovoz.app.domain.general.model.product.ProductsSectionModel
import com.vodovoz.app.domain.general.model.product.ProductsSectionUi
import com.vodovoz.app.domain.general.model.product.toUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.home.model.toParentCategory
import com.vodovoz.app.feature.product_catalog.ProductCatalogFragment.DataSource
import com.vodovoz.app.feature.product_comments.model.SortUi
import com.vodovoz.app.feature.product_comments.model.toDomain
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.PagingProductsMviViewModel
import com.vodovoz.app.ui.paging.PagingState
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ProductCatalogViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
) : PagingProductsMviViewModel<ProductUi, ProductCatalogViewModel.ProductCatalogState, ProductCatalogViewModel.ProductCatalogEvent>(
    state = ProductCatalogState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    val dataSource = savedState.get<DataSource>("dataSource") ?: DataSource.Missing

    init {
        setupScreen().invokeOnCompletion {
            fetchProductListData()
        }
    }

    private fun setupScreen() = viewModelScope.launch {
        if (dataSource is DataSource.Category) {
            _state.update { s ->
                val currentCategory = CategoryUi(id = dataSource.categoryId.toInt(), name = "")
                s.copy(
                    currentCategory = currentCategory,
                    showFilters = true,
                    currentBottomSheetCategory = currentCategory.toParentCategory()
                )
            }
        } else {
            _state.update { s ->
                s.copy(
                    showEmptyCategory = true,
                    showFilters = false
                )
            }
        }
    }


    private fun fetchProductListData() = viewModelScope.launch {
        if (stateSnapshot.productsSection == ProductsSectionUi.Empty) {
            _state.update { s ->
                s.copy(uiState = ProductCatalogUiState.Loading)
            }
        }

        val categoryId = stateSnapshot.currentCategory.id
        val sortModel = stateSnapshot.currentSort.toDomain()

        when (dataSource) {
            is DataSource.Brand -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getBrandProducts(
                            dataSource.brandId,
                            sortModel,
                            categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getBrandProductsPaged(
                            dataSource.brandId,
                            sortModel,
                            categoryId
                        )
                    }
                )
            }

            is DataSource.ButtonProducts -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getAllSuperTop(
                            buttonId = dataSource.buttonId,
                            categoryId = categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getAllSuperTopPaged(
                            buttonId = dataSource.buttonId,
                            categoryId = categoryId,
                            sort = sortModel
                        )
                    }
                )
            }

            DataSource.HurryBuyUpProducts -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getAllHurryUpBuyProducts(
                            categoryId = categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getAllHurryUpBuyProductsPaged(
                            categoryId = categoryId,
                            sort = sortModel
                        )
                    }
                )
            }

            DataSource.NewProducts -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getAllNewProducts(
                            categoryId = categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getAllNewProductsPaged(
                            categoryId = categoryId,
                            sort = sortModel
                        )
                    }
                )
            }

            DataSource.ViewedProducts -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getAllViewedProducts(
                            categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getAllViewedProductsPaged(
                            categoryId, sortModel
                        )
                    }
                )
            }

            is DataSource.Products -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getBannerProducts(
                            dataSource.bannerId,
                            dataSource.blockId,
                            sortModel,
                            categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getBannerProductsPaged(
                            dataSource.bannerId,
                            dataSource.blockId,
                            sortModel,
                            categoryId
                        )
                    }
                )
            }

            is DataSource.Search -> {
                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getSearchProducts(
                            dataSource.query,
                            categoryId
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getSearchProductsPaged(
                            dataSource.query,
                            categoryId,
                            sortModel
                        )
                    }
                )
            }

            is DataSource.Category -> {
                val currentFilters = extractSelectedFilters()

                fetchProductsData(
                    fetchProductsSection = {
                        vodovozServiceRepository.getCategoryProducts(
                            categoryId.toLong(),
                            currentFilters
                        ).singleResult()
                    },
                    fetchPagedProductsFlow = {
                        vodovozServiceRepository.getCategoryProductsPaged(
                            categoryId.toLong(),
                            sortModel,
                            currentFilters
                        )
                    }
                )
            }


            DataSource.Missing -> {
                navigateBack()
            }

        }

    }

    private fun extractSelectedFilters(): FiltersModel {
        val dataStateFilters = stateSnapshot.currentFilters

        val filteredFilters = dataStateFilters.filters.filter { filterUi ->
            filterUi.values.any { filterValueUi -> filterValueUi.selected } || (filterUi.bounds != null && filterUi.currentBounds != null)
        }.map { filterUi ->
            filterUi.copy(values = filterUi.values.filter { value -> value.selected })
        }

        val price = if (dataStateFilters == FiltersUi.Empty) {
            FiltersPriceUi(0, Int.MAX_VALUE)
        } else {
            dataStateFilters.price
        }

        return dataStateFilters.copy(
            filters = filteredFilters,
            price = price
        ).toDomain()
    }


    fun refresh() = viewModelScope.launch {
        if (stateSnapshot.uiState is ProductCatalogUiState.Loading) return@launch
        _state.update { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchProductListData().join()

        _state.update { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun showSortBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showSortBottomSheet = true
            )
        }
    }

    fun hideSortBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showSortBottomSheet = false
            )
        }
    }


    private suspend fun fetchProductsData(
        fetchProductsSection: suspend () -> Result<ProductsSectionModel>,
        fetchPagedProductsFlow: () -> Flow<PagingData<ProductModel>>,
    ) {


        val canViewAdultProducts =
            userPreferencesRepository.canViewAdultProducts.firstOrNull() ?: false
        val categoriesTreeJob = fetchCategoriesTree(stateSnapshot.currentCategory.id.toLong())
        val productsSectionResult =
            fetchProductsSection().map { productsSectionModel ->
                productsSectionModel.toUi()
            }


        productsSectionResult.onSuccess { productsSection ->

            categoriesTreeJob.join()

            _state.update { state ->

                val categoryTreeList: List<CategoryUi> = buildList {
                    addAll(stateSnapshot.categoryTree.allCategories()
                        .map { category -> category.toCategory() }
                    )
                    removeIf { categoryUi -> categoryUi.id == state.currentCategory.id }
                }

                val categories = if (dataSource is DataSource.Category) {
                    categoryTreeList
                } else {
                    productsSection.categories.ifEmpty { categoryTreeList }
                }.takeIf { it.size > 1 } ?: emptyList()

                state.copy(
                    productsSection = productsSection.copy(categories = categories),
                    uiState = if (productsSection.forAdults != null && !canViewAdultProducts) {
                        ProductCatalogUiState.ForAdults(productsSection.forAdults)
                    } else {
                        ProductCatalogUiState.Body
                    },
                    currentSort = state.currentSort.takeIf { sort ->
                        sort != SortUi.Empty
                    } ?: productsSection.sorting.firstOrNull() ?: SortUi.Empty,
                    currentBottomSheetCategory = state.currentCategory.toParentCategory(),
                    categoryTree = if (dataSource !is DataSource.Category) productsSection.categories.map { categoryUi ->
                        categoryUi.toParentCategory()
                    } else state.categoryTree,
                    showShare = "${productsSection.share.url}${productsSection.share.text}".isNotBlank()
                )
            }

            viewModelScope.launch {
                fetchPagedProductsFlow().map { pagingData ->
                    pagingData.map { productModel -> productModel.toUi() }
                }.collectPagingData()
            }

        }.onFailure { t ->
            val uiState = when (t) {
                is EmptyResultException -> ProductCatalogUiState.Empty(
                    t.placeholder?.toUi() ?: VodovozPlaceholderUi.Empty
                )


                else -> ProductCatalogUiState.Error
            }


            if (uiState is ProductCatalogUiState.Empty && stateSnapshot.productsSection == ProductsSectionUi.Empty) {
                _state.update { s ->
                    s.copy(uiState = uiState)
                }
            } else if (stateSnapshot.productsSection != ProductsSectionUi.Empty) {
                _state.update { s ->
                    s.copy(
                        loadStates = s.loadStates.copy(
                            refresh = LoadState.Error(t)
                        ),
                        uiState = ProductCatalogUiState.Body,
                        productsSection = s.productsSection.copy(
                            productsQuantityText = ""
                        )
                    )
                }
            } else {
                _state.update { s ->
                    s.copy(uiState = uiState)
                }
            }
        }
    }

    fun selectSort(sort: SortUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentSort = sort,
                showSortBottomSheet = false,
                items = emptyList()
            )
        }
        sendEvent(ProductCatalogEvent.ScrollToTop)
        fetchProductListData()
    }

    fun switchLayout() = viewModelScope.launch {
        _state.update { s ->
            s.copy(isGridView = !s.isGridView)
        }
    }

    private fun fetchCategoriesTree(categoryId: Long) = viewModelScope.launch {
        if (stateSnapshot.currentBottomSheetCategory.takeIf { it.id == categoryId && it.countChildren == 0 } != null) return@launch
        if (
            stateSnapshot.categoryTree.allCategories().firstOrNull {
                it.id == categoryId && it.countChildren == 0
            } != null
        ) return@launch

        if (dataSource !is DataSource.Category) return@launch

        val categoryTreeResult =
            vodovozServiceRepository.getCategoryTree(categoryId).singleResult()


        categoryTreeResult.onSuccess { categoryTree ->
            val backendCategories = categoryTree.map { category ->
                category.toUi()
            }

            val parentOfOnlyLeaf = backendCategories.findParentOfOnlyLeaf()
            val childrenCategoriesOfParent = if (parentOfOnlyLeaf != null) {
                vodovozServiceRepository.getCategoryTree(parentOfOnlyLeaf.id).singleResult()
                    .getOrNull()?.map { it.toUi() } ?: backendCategories
            } else backendCategories

            _state.update { s ->

                s.copy(categoryTree = childrenCategoriesOfParent)
            }
        }.onFailure {
            if (stateSnapshot.categoryTree.isEmpty()) {
                _state.update { s ->
                    s.copy(showCategoriesBottomSheet = false)
                }
            }
        }
    }

    fun selectBottomSheetCategory(category: ParentCategoryUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(currentBottomSheetCategory = category)
        }

        fetchCategoriesTree(categoryId = category.id)
    }

    fun selectCategory(category: CategoryUi) = viewModelScope.launch {
        val newCategory =
            if (category == stateSnapshot.currentCategory && dataSource !is DataSource.Category) CategoryUi.Empty
            else if (category == stateSnapshot.currentCategory) return@launch
            else category

        _state.update { s ->
            s.copy(
                currentCategory = newCategory,
                loadStates = s.loadStates.copy(refresh = LoadState.Loading),
                items = emptyList(),
                currentFilters = FiltersUi.Empty,
                showCategoriesBottomSheet = false,
                currentBottomSheetCategory = newCategory.toParentCategory()
            )
        }
        sendEvent(ProductCatalogEvent.ScrollToTop)
        fetchProductListData()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoBack)
    }

    fun navigateToSearch(query: String) = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoToSearch(query))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoToProductDetails(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToProductFilters() = viewModelScope.launch {
        with(stateSnapshot) {
            sendEvent(
                ProductCatalogEvent.GoToProductFilters(
                    currentCategory.id.toLong(),
                    currentFilters
                )
            )
        }
    }

    fun changeFilters(filters: FiltersUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                currentFilters = filters,
                loadStates = s.loadStates.copy(refresh = LoadState.Loading),
                items = emptyList()
            )
        }
        sendEvent(ProductCatalogEvent.ScrollToTop)
        fetchProductListData()
    }

    fun shareProducts() = viewModelScope.launch {
        val share = stateSnapshot.productsSection.share
        sendEvent(
            ProductCatalogEvent.Share(
                resourcesProvider.getString(R.string.share, share.text, share.url)
            )
        )
    }

    fun showCategoriesBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showCategoriesBottomSheet = true,
                currentBottomSheetCategory = s.currentCategory.toParentCategory()
            )
        }
    }

    fun hideCategoriesBottomSheet() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showCategoriesBottomSheet = false
            )
        }
    }

    fun chooseBottomSheetCategory() = viewModelScope.launch {
        val currentCategory = stateSnapshot.currentBottomSheetCategory.toCategory()
        selectCategory(currentCategory)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoToProductAnalogs(product.id))
    }

    fun navigateToSpeech() = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoToSpeech)
    }

    fun navigateToQrCode() = viewModelScope.launch {
        sendEvent(ProductCatalogEvent.GoToQrCode)
    }

    fun setCanViewAdultProducts() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = ProductCatalogUiState.Body)
        }
        userPreferencesRepository.setCanViewAdultProducts(true)
    }

    @Immutable
    data class ProductCatalogState(
        @Stable
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        override val items: List<ProductUi> = emptyList(),
        val productsSection: ProductsSectionUi = ProductsSectionUi.Empty,
        val categoryTree: List<ParentCategoryUi> = emptyList(),
        val currentCategory: CategoryUi = CategoryUi.Empty,
        val currentBottomSheetCategory: ParentCategoryUi = ParentCategoryUi.Empty,
        val currentFilters: FiltersUi = FiltersUi.Empty,
        val currentSort: SortUi = SortUi.Empty,
        val uiState: ProductCatalogUiState = ProductCatalogUiState.Loading,
        val isGridView: Boolean = true,
        val showFilters: Boolean = false,
        val showCategoryList: Boolean = false,
        val showCategoriesBottomSheet: Boolean = false,
        val showSortBottomSheet: Boolean = false,
        val showRefreshIndicator: Boolean = false,
        val showEmptyCategory: Boolean = false,
        val showShare: Boolean = false,
    ) : PagingState<ProductUi, ProductCatalogState>() {

        override fun copyPagingState(
            items: List<ProductUi>,
            loadStates: CombinedLoadStates,
        ): ProductCatalogState = copy(items = items, loadStates = loadStates)

    }

    @Stable
    sealed interface ProductCatalogUiState {
        data class Empty(val placeholder: VodovozPlaceholderUi) : ProductCatalogUiState
        data object Error : ProductCatalogUiState
        data object Loading : ProductCatalogUiState
        data object Body : ProductCatalogUiState
        data class ForAdults(val forAdultsUi: ForAdultsUi) : ProductCatalogUiState
    }

    sealed class ProductCatalogEvent : Event {
        data object GoBack : ProductCatalogEvent()
        data object ScrollToTop : ProductCatalogEvent()
        data object GoToSpeech : ProductCatalogEvent()
        data object GoToQrCode : ProductCatalogEvent()

        data class GoToSearch(val query: String) : ProductCatalogEvent()
        data class GoToCategories(
            val categories: List<CategoryUi>,
            val currentCategory: CategoryUi,
        ) : ProductCatalogEvent()

        data class GoToProductDetails(val productId: Long) : ProductCatalogEvent()
        data class GoToProductFilters(val categoryId: Long, val filters: FiltersUi) :
            ProductCatalogEvent()

        data class Share(val text: String) : ProductCatalogEvent()
        data class GoToProductAnalogs(val productId: Long) : ProductCatalogEvent()
    }
}