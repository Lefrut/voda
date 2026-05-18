package com.m.vodovoz.feature.all.orders.history

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.cart.change
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException

import com.m.vodovoz.domain.general.model.order.OrdersHistoryDetailsModel
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryItemUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryTabUi
import com.m.vodovoz.feature.all.orders.history.model.mapToUi
import com.m.vodovoz.feature.all.orders.history.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingProductsMviViewModel2
import com.m.vodovoz.ui.paging.PagingState2
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import com.m.vodovoz.util.extensions.debounceWithMax
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appmetrica.analytics.impl.ba
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrdersHistoryViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : PagingProductsMviViewModel2<OrdersHistoryItemUi, ProductUi, OrdersHistoryViewModel.AllOrdersState, OrdersHistoryViewModel.AllOrdersEvent>(
    state = AllOrdersState(),
    blockedProductsFlow = cartManager.blockedProductsFlow,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val querySharedFlow = MutableSharedFlow<String>(0)
    private var ordersPagingJob: Job? = null
    private var productsPagingJob: Job? = null

    init {
        handleQueries()
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchOrdersHistoryDetails()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleQueries() =
        querySharedFlow.debounceWithMax(300L, 6).filter { stateSnapshot.searchMode }.mapLatest {
            fetchOrdersHistoryDetails()
        }.launchIn(viewModelScope)


    fun fetchOrdersHistoryDetails() = viewModelScope.launch {

        if (stateSnapshot.uiState !is AllOrdersUiState.Body) {
            updateState { s ->
                s.copy(uiState = AllOrdersUiState.Loading)
            }
        }

        ordersPagingJob?.cancel()
        productsPagingJob?.cancel()
        updateState { s ->
            s.copy(
                items2 = emptyList(),
                loadStates2 = emptyCombinedLoadStates,
                productsTitle = ""
            )
        }

        val ordersPagingResult = vodovozServiceRepository.getOrdersHistoryItemsPagingResult(
            selectedTabId = stateSnapshot.currentTab?.id,
            year = stateSnapshot.currentYear,
            searchQuery = stateSnapshot.searchQuery
        )

        ordersPagingJob = viewModelScope.launch {
            launch {
                ordersPagingResult.flow.map { pagingData ->
                    pagingData.map { historyItemModel ->
                        historyItemModel.toUi()
                    }
                }.collectPagingData1()
            }
            launch {
                ordersPagingResult.meta
                    .map { meta -> meta.firstPageData }
                    .filterNotNull()
                    .collect { ordersHistoryDetails ->
                        applyOrdersHistoryDetails(ordersHistoryDetails)
                    }
            }
            launch {
                ordersPagingResult.meta
                    .map { meta -> meta.pagesInfo.endOfPaginationReached }
                    .distinctUntilChanged()
                    .filter { endOfPaginationReached -> endOfPaginationReached }
                    .collect {
                        fetchBestForYouProducts()
                    }
            }
            launch {
                state.map { s -> s.loadStates1.refresh }
                    .filterIsInstance<LoadState.Error>()
                    .collect { loadState ->
                        applyOrdersHistoryError(loadState.error)
                    }
            }
        }
    }

    private fun applyOrdersHistoryDetails(ordersHistoryDetails: OrdersHistoryDetailsModel) {
        if (ordersHistoryDetails.placeholder != null && ordersHistoryDetails.tabs.isEmpty()) {
            updateState { s ->
                s.copy(
                    title = ordersHistoryDetails.title,
                    uiState = AllOrdersUiState.Empty(ordersHistoryDetails.placeholder.toUi()),
                    showRefreshIndicator = false
                )
            }
            return
        }

        val tabs = ordersHistoryDetails.tabs.mapToUi().mergeYearsWith(stateSnapshot.tabs)
        val preferredTabId = ordersHistoryDetails.activeTabId.takeIf {
            stateSnapshot.tabs.isEmpty()
        }
        val selectedTabIndex = tabs.resolveSelectedTabIndex(
            currentIndex = stateSnapshot.selectedTabIndex,
            preferredTabId = preferredTabId
        )

        val selectedTab = tabs.getOrNull(selectedTabIndex)

        updateState { s ->
            s.copy(
                title = ordersHistoryDetails.title,
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                currentTabPlaceholder = selectedTab?.placeholder,
                banners = ordersHistoryDetails.banners.mapToUi(),
                uiState = AllOrdersUiState.Body,
                showRefreshIndicator = false
            )
        }
    }

    private fun applyOrdersHistoryError(t: Throwable) {
        val uiState = when {
            t is EmptyResultException && t.placeholder != null -> AllOrdersUiState.Empty(t.placeholder.toUi())
            else -> AllOrdersUiState.Error
        }

        updateState { s ->
            s.copy(
                uiState = uiState,
                showRefreshIndicator = false
            )
        }
    }

    fun changeMode(searchMode: Boolean) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                searchMode = searchMode,
                searchQuery = "",
                currentYear = null,
            )
        }

        if (!searchMode) {
            fetchOrdersHistoryDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoBack)
    }


    fun changeSearchQuery(query: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(searchQuery = query)
        }
        querySharedFlow.emit(query)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToCatalog)
    }

    fun selectTab(index: Int) = viewModelScope.launch {
        if (index == stateSnapshot.selectedTabIndex) return@launch

        updateState { s ->
            s.copy(
                selectedTabIndex = index,
                currentYear = null,
                currentTabPlaceholder = s.tabs.getOrNull(index)?.placeholder,
                items1 = emptyList(),
                items2 = emptyList(),
                loadStates1 = emptyCombinedLoadStates,
                loadStates2 = emptyCombinedLoadStates,
                productsTitle = ""
            )
        }

        fetchOrdersHistoryDetails()
    }

    fun selectYear(year: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentYear = if (year == s.currentYear) null else year,
                items1 = emptyList(),
                items2 = emptyList(),
                loadStates1 = emptyCombinedLoadStates,
                loadStates2 = emptyCombinedLoadStates,
                productsTitle = ""
            )
        }

        fetchOrdersHistoryDetails()
    }

    fun navigateToOrderDetails(item: OrdersHistoryItemUi) = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToOrderDetails(item.id))
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToProductDetails(product.id))
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToProductAnalogs(product.id))
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }

    private fun fetchBestForYouProducts() {
        if (productsPagingJob?.isActive == true) return
        productsPagingJob = viewModelScope.launch {
            val productsPagingResult = vodovozServiceRepository.getBestForYouProductsPagingResult()

            launch {
                productsPagingResult.flow.map { pagingData ->
                    pagingData.map { productModel ->
                        productModel.toUi()
                    }
                }.collectPagingData2()
            }

            launch {
                productsPagingResult.meta
                    .map { meta ->
                        meta.firstPageData?.title
                    }
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collect { title ->
                        updateState { s ->
                            s.copy(productsTitle = title)
                        }
                    }
            }
        }
    }

    fun activateOrderItemButton(ordersHistoryItem: OrdersHistoryItemUi) = viewModelScope.launch {

        val button = ordersHistoryItem.button ?: return@launch

        when {
            button.id == "povtorit" -> {
                vodovozServiceRepository.repeatOrder(ordersHistoryItem.id).singleResult()
                    .onSuccess {
                        cartManager.updateRefreshCart(true)
                        sendEvent(AllOrdersEvent.GoToCart)
                    }
            }

            button.url.isNotEmpty() -> {

                val event = when {
                    button.browserUrl -> AllOrdersEvent.OpenUrl(button.url)
                    else -> AllOrdersEvent.GoToWebView(button.url)
                }


                sendEvent(event)
            }

            else -> {

            }
        }
    }

    fun refresh() = viewModelScope.launch {
        updateState { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchOrdersHistoryDetails()

        delay(250L)
        updateState { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun activateBanner(banner: BannerUi) = viewModelScope.launch {
        sendEvent(AllOrdersEvent.ActivateBanner(banner))
    }

    fun showAboutAdvertisingBS(aboutAdvertisingUi: AboutAdvertisingUi) {
        updateState { s ->
            s.copy(
                aboutAdvertisingBS = aboutAdvertisingUi
            )
        }
    }

    fun closeAboutAdvertisingBS() {
        updateState { s ->
            s.copy(
                aboutAdvertisingBS = null
            )
        }
    }

    @Immutable
    data class AllOrdersState(
        val title: String = "",
        val searchQuery: String = "",
        val uiState: AllOrdersUiState = AllOrdersUiState.Loading,
        val searchMode: Boolean = false,
        val selectedTabIndex: Int = 0,
        val tabs: List<OrdersHistoryTabUi> = emptyList(),
        val currentYear: String? = null,
        val currentTabPlaceholder: VodovozPlaceholderUi? = null,
        val banners: List<BannerUi> = emptyList(),
        override val items1: List<OrdersHistoryItemUi> = emptyList(),
        override val items2: List<ProductUi> = emptyList(),
        override val loadStates1: CombinedLoadStates = emptyCombinedLoadStates,
        override val loadStates2: CombinedLoadStates = emptyCombinedLoadStates,
        val showRefreshIndicator: Boolean = false,
        val aboutAdvertisingBS: AboutAdvertisingUi? = null,
        val productsTitle: String = "",
    ) : PagingState2<OrdersHistoryItemUi, ProductUi, AllOrdersState>() {


        val currentTab: OrdersHistoryTabUi?
            get() = tabs.getOrNull(selectedTabIndex)

        override fun copyPagingState(
            items1: List<OrdersHistoryItemUi>,
            items2: List<ProductUi>,
            loadStates1: CombinedLoadStates,
            loadStates2: CombinedLoadStates,
        ): AllOrdersState = copy(
            items1 = items1,
            items2 = items2,
            loadStates1 = loadStates1,
            loadStates2 = loadStates2
        )

    }

    sealed class AllOrdersEvent : Event {
        data object GoToCart : AllOrdersEvent()
        data object GoBack : AllOrdersEvent()
        data object GoToCatalog : AllOrdersEvent()

        data class GoToOrderDetails(val id: Long) : AllOrdersEvent()
        data class GoToProductDetails(val id: Long) : AllOrdersEvent()
        data class GoToProductAnalogs(val id: Long) : AllOrdersEvent()
        data class OpenUrl(val url: String) : AllOrdersEvent()
        data class GoToWebView(val url: String) : AllOrdersEvent()
        data class ActivateBanner(val banner: BannerUi) : AllOrdersEvent()
    }

    @Stable
    sealed interface AllOrdersUiState {
        data object Loading : AllOrdersUiState
        data object Error : AllOrdersUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) :
            AllOrdersUiState

        data object Body : AllOrdersUiState
    }
}

private fun List<OrdersHistoryTabUi>.resolveSelectedTabIndex(
    currentIndex: Int,
    preferredTabId: String?,
): Int {
    if (isEmpty()) return 0

    val preferredIndex = preferredTabId
        ?.takeIf { it.isNotBlank() }
        ?.let { id -> indexOfFirst { it.id == id } }
        ?.takeIf { it in indices }

    return preferredIndex ?: currentIndex.takeIf { it in indices } ?: 0
}

private fun List<OrdersHistoryTabUi>.mergeYearsWith(
    oldTabs: List<OrdersHistoryTabUi>,
): List<OrdersHistoryTabUi> {
    return map { tab ->
        if (tab.years.isNotEmpty()) {
            tab
        } else {
            val oldTab = oldTabs.firstOrNull { it.id == tab.id }
            tab.copy(
                years = oldTab?.years.orEmpty(),
                selectedYear = tab.selectedYear ?: oldTab?.selectedYear
            )
        }
    }
}
