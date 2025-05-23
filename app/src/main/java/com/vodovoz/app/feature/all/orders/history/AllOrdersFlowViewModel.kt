package com.vodovoz.app.feature.all.orders.history

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.map
import com.vodovoz.app.BuildConfig
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
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.all.orders.history.model.OrderFilterUi
import com.vodovoz.app.feature.all.orders.history.model.OrdersHistoryItemUi
import com.vodovoz.app.feature.all.orders.history.model.mapToUi
import com.vodovoz.app.feature.all.orders.history.model.toUi
import com.vodovoz.app.mapper.OrderListMapper.mapToUI
import com.vodovoz.app.ui.model.custom.OrdersFiltersBundleUI
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.ui.paging.copy
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.debounceWithMax
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AllOrdersFlowViewModel @Inject constructor(
    private val repository: MainRepository,
    private val accountManager: AccountManager,
//    private val dataRepository: DataRepository,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<AllOrdersFlowViewModel.AllOrdersState, AllOrdersFlowViewModel.AllOrdersEvent>(
    AllOrdersState()
) {

    private val querySharedFlow = MutableSharedFlow<String>(0)

    private val pagingProductsListener = PagingDataListener<OrdersHistoryItemUi>(
        onUpdateItems = { itemSnapshotList ->
            uiStateListener.updateData { s ->
                val pagedItems = itemSnapshotList.mapNotNull { product ->
                    product
                }
                s.copy(items = pagedItems)
            }
        }
    )

    init {
        handleQueries()
        listenProductsLoadStates()
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchOrdersHistoryDetails()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleQueries() =
        querySharedFlow.debounceWithMax(200L, 5).filter { dataState.searchMode }.mapLatest {
            fetchOrdersHistoryDetails()
        }.launchIn(viewModelScope)

    private fun listenProductsLoadStates() = viewModelScope.launch {
        pagingProductsListener.collectLoadState { combinedLoadStates ->
            val refreshState = when {
                combinedLoadStates.refresh is LoadState.Loading && dataState.items.isNotEmpty() -> {
                    dataState.loadStates.refresh
                }

                else -> combinedLoadStates.refresh
            }

            uiStateListener.updateData { s ->
                s.copy(loadStates = combinedLoadStates.copy(refresh = refreshState))
            }
        }

    }


    fun notifyPagingItems(index: Int) = viewModelScope.launch {
        pagingProductsListener[index]
    }


    fun fetchOrdersHistoryDetails() = viewModelScope.launch {
        if (dataState.uiState !is AllOrdersUiState.Body) {
            uiStateListener.updateData { s ->
                s.copy(uiState = AllOrdersUiState.Loading)
            }
        }

        val ordersHistoryDetailsResult =
            vodovozServiceRepository.getOrdersHistoryDetails().singleResult()

        ordersHistoryDetailsResult.onSuccess { ordersHistoryDetails ->


            uiStateListener.updateData { s ->
                s.copy(
                    title = ordersHistoryDetails.title,
                    filters = ordersHistoryDetails.filters.mapToUi(),
                    uiState = AllOrdersUiState.Body
                )
            }

            vodovozServiceRepository.getOrdersHistoryItemsPaged(
                dataState.currentFilters.joinToString(",") { it.id },
                dataState.searchQuery
            ).collectLatest { pagingData ->
                val pg = pagingData.map { historyItemModel ->
                    historyItemModel.toUi()
                }
                pagingProductsListener.collectPagingData(pg)
            }


        }.onFailure { t ->

            val uiState = when {
                t is EmptyResultException && t.placeholder != null -> AllOrdersUiState.Empty(t.placeholder.toUi())
                else -> AllOrdersUiState.Error
            }

            if (uiState is AllOrdersUiState.Body) return@onFailure

            uiStateListener.updateData { s ->
                s.copy(uiState = uiState)
            }

        }
    }

    private fun fetchAllOrders() {
        val userId =
            accountManager.fetchAccountId() ?: return
        viewModelScope.launch {
            flow {
                emit(
                    repository.fetchAllOrders(
                        userId = userId,
                        page = state.page,
                        appVersion = BuildConfig.VERSION_NAME,
                        orderId = state.data.ordersFiltersBundleUI.orderId,
                        status = StringBuilder().apply {
                            state.data.ordersFiltersBundleUI.orderFilterUIList.forEach {
                                if (it.isChecked) {
                                    append(it.id).append(
                                        ","
                                    )
                                }
                            }
                        }.toString()
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        uiStateListener.value = if (data.orders.isEmpty() && !state.loadMore) {
                            state.copy(
                                error = ErrorState.Empty(),
                                loadingPage = false,
                                loadMore = false,
                                bottomItem = null,
                                page = 1,
                                data = state.data.copy(
                                    errorTitle = data.title,
                                    errorMessage = data.message,
                                    itemsList = listOf()
                                )
                            )
                        } else {

                            val itemsList = if (state.loadMore) {
                                state.data.itemsList + data.orders
                            } else {
                                data.orders
                            }

                            state.copy(
                                page = if (data.orders.isEmpty()) null else state.page?.plus(1),
                                loadingPage = false,
                                data = state.data.copy(
                                    itemsList = itemsList,
                                    ordersFiltersBundleUI = if (state.data.ordersFiltersBundleUI.orderFilterUIList.isEmpty()) {
                                        OrdersFiltersBundleUI().apply {
                                            orderFilterUIList.addAll(data.filters)
                                        }
                                    } else {
                                        state.data.ordersFiltersBundleUI
                                    }
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
                    debugLog { "fetch all orders sorted error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun firstLoadSorted() {
        if (!state.isFirstLoad) {
            uiStateListener.value =
                state.copy(isFirstLoad = true, loadingPage = true)
            fetchAllOrders()
        }
    }

    fun refreshSorted() {
        uiStateListener.value =
            state.copy(loadingPage = true, page = 1, loadMore = false, bottomItem = null)
        fetchAllOrders()
    }

    fun loadMoreSorted() {
        if (state.bottomItem == null && state.page != null) {
            uiStateListener.value = state.copy(loadMore = true, bottomItem = BottomProgressItem())
            fetchAllOrders()
        }
    }

    fun updateFilterBundle(filterBundle: OrdersFiltersBundleUI) {
        var filterCount = if (filterBundle.orderId != null) 1 else 0
        filterBundle.orderFilterUIList.forEach {
            if (it.isChecked) {
                filterCount++
            }
        }
        uiStateListener.value = state.copy(
            data = state.data.copy(
                ordersFiltersBundleUI = filterBundle,
                filterCount = filterCount
            ),
            page = 1,
            loadMore = false,
            loadingPage = true
        )
        fetchAllOrders()
    }

    fun repeatOrder(orderId: Long) {
        val userId =
            accountManager.fetchAccountId() ?: return
        uiStateListener.value = state.copy(loadingPage = true, error = null)
        viewModelScope.launch {
            flow {
                emit(
                    repository.repeatOrder(
                        userId = userId,
                        orderId = orderId
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        cartManager.updateCartListState(true)
                        uiStateListener.value = state.copy(loadingPage = false, error = null)
                        eventListener.emit(AllOrdersEvent.GoToCart)
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "repeat order error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun isLoginAlready() = accountManager.isAlreadyLogin()

    fun changeMode(searchMode: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                searchMode = searchMode,
                searchQuery = ""
            )
        }

        if (!searchMode) {
            fetchOrdersHistoryDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(AllOrdersEvent.GoBack)
    }


    fun changeSearchQuery(query: String) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(searchQuery = query)
        }
        querySharedFlow.emit(query)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        eventListener.emit(AllOrdersEvent.GoToCatalog)
    }

    fun selectAllFilters() = viewModelScope.launch {
        if (dataState.currentFilters.isEmpty()) return@launch

        uiStateListener.updateData { s ->
            s.copy(currentFilters = emptyList())
        }

        fetchOrdersHistoryDetails()
    }

    fun selectFilter(filter: OrderFilterUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            val currentFilters = s.currentFilters
            s.copy(
                currentFilters = if (currentFilters.contains(filter)) currentFilters.minus(filter)
                else listOf(filter)
            )
        }

        fetchOrdersHistoryDetails()
    }

    fun navigateToOrderDetails(item: OrdersHistoryItemUi) = viewModelScope.launch {
        eventListener.emit(AllOrdersEvent.GoToOrderDetails(item.id))
    }

    fun activateOrderItemButton(ordersHistoryItem: OrdersHistoryItemUi) = viewModelScope.launch {

    }

    @Immutable
    data class AllOrdersState(
        val itemsList: List<Item> = emptyList(),
        val ordersFiltersBundleUI: OrdersFiltersBundleUI = OrdersFiltersBundleUI(),
        val filterCount: Int = 0,
        val errorTitle: String = "",
        val errorMessage: String = "",

        val title: String = "",
        val searchQuery: String = "",
        val uiState: AllOrdersUiState = AllOrdersUiState.Loading,
        val searchMode: Boolean = false,
        val currentFilters: List<OrderFilterUi> = emptyList(),
        val filters: List<OrderFilterUi> = emptyList(),
        val items: List<OrdersHistoryItemUi> = emptyList(),
        val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
    ) : State

    sealed class AllOrdersEvent : Event {
        data object GoToCart : AllOrdersEvent()
        data object GoBack : AllOrdersEvent()
        data object GoToCatalog : AllOrdersEvent()

        data class GoToOrderDetails(val id: Long) : AllOrdersEvent()
    }

    @Immutable
    sealed interface AllOrdersUiState {
        data object Loading : AllOrdersUiState
        data object Error : AllOrdersUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AllOrdersUiState
        data object Body : AllOrdersUiState
    }
}