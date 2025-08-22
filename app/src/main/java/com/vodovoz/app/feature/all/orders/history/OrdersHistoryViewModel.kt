package com.vodovoz.app.feature.all.orders.history

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.exceptions.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.all.orders.history.model.OrderFilterUi
import com.vodovoz.app.feature.all.orders.history.model.OrdersHistoryItemUi
import com.vodovoz.app.feature.all.orders.history.model.mapToUi
import com.vodovoz.app.feature.all.orders.history.model.toUi
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.PagingMviViewModel
import com.vodovoz.app.ui.paging.PagingState
import com.vodovoz.app.ui.paging.emptyCombinedLoadStates
import com.vodovoz.app.util.extensions.debounceWithMax
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrdersHistoryViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingMviViewModel<OrdersHistoryItemUi, OrdersHistoryViewModel.AllOrdersState, OrdersHistoryViewModel.AllOrdersEvent>(
    AllOrdersState()
) {

    private val querySharedFlow = MutableSharedFlow<String>(0)

    init {
        handleQueries()
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchOrdersHistoryDetails()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleQueries() =
        querySharedFlow.debounceWithMax(200L, 5).filter { stateSnapshot.searchMode }.mapLatest {
            fetchOrdersHistoryDetails()
        }.launchIn(viewModelScope)


    fun fetchOrdersHistoryDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is AllOrdersUiState.Body) {
            _state.update { s ->
                s.copy(uiState = AllOrdersUiState.Loading)
            }
        }

        val ordersHistoryDetailsResult =
            vodovozServiceRepository.getOrdersHistoryDetails().singleResult()

        ordersHistoryDetailsResult.onSuccess { ordersHistoryDetails ->


            _state.update { s ->
                s.copy(
                    title = ordersHistoryDetails.title,
                    filters = ordersHistoryDetails.filters.mapToUi(),
                    uiState = AllOrdersUiState.Body,
                    showRefreshIndicator = false
                )
            }

            vodovozServiceRepository.getOrdersHistoryItemsPaged(
                stateSnapshot.currentFilters.joinToString(",") { it.id },
                stateSnapshot.searchQuery
            ).collectLatest { pagingData ->
                val pg = pagingData.map { historyItemModel ->
                    historyItemModel.toUi()
                }
                collectPagingData(pg)
            }


        }.onFailure { t ->

            val uiState = when {
                t is EmptyResultException && t.placeholder != null -> AllOrdersUiState.Empty(t.placeholder.toUi())
                else -> AllOrdersUiState.Error
            }

            if (uiState is AllOrdersUiState.Body) return@onFailure

            _state.update { s ->
                s.copy(uiState = uiState)
            }

        }
    }

    fun changeMode(searchMode: Boolean) = viewModelScope.launch {
        _state.update { s ->
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
        sendEvent(AllOrdersEvent.GoBack)
    }


    fun changeSearchQuery(query: String) = viewModelScope.launch {
        _state.update { s ->
            s.copy(searchQuery = query)
        }
        querySharedFlow.emit(query)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToCatalog)
    }

    fun selectAllFilters() = viewModelScope.launch {
        if (stateSnapshot.currentFilters.isEmpty()) return@launch

        _state.update { s ->
            s.copy(currentFilters = emptyList())
        }

        fetchOrdersHistoryDetails()
    }

    fun selectFilter(filter: OrderFilterUi) = viewModelScope.launch {
        _state.update { s ->
            val currentFilters = s.currentFilters
            s.copy(
                currentFilters = if (currentFilters.contains(filter)) currentFilters.minus(filter)
                else (currentFilters + listOf(filter)).distinct()
            )
        }

        fetchOrdersHistoryDetails()
    }

    fun navigateToOrderDetails(item: OrdersHistoryItemUi) = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToOrderDetails(item.id))
    }

    fun activateOrderItemButton(ordersHistoryItem: OrdersHistoryItemUi) = viewModelScope.launch {

        val button = ordersHistoryItem.button ?: return@launch

        when {
            button.id == "povtorit" -> {
                vodovozServiceRepository.repeatOrder(ordersHistoryItem.id).singleResult()
                    .onSuccess {
                        cartManager.updateCartListState(true)
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
        _state.update { s ->
            s.copy(showRefreshIndicator = true)
        }

        fetchOrdersHistoryDetails().join()

        _state.update { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    @Immutable
    data class AllOrdersState(
        val title: String = "",
        val searchQuery: String = "",
        val uiState: AllOrdersUiState = AllOrdersUiState.Loading,
        val searchMode: Boolean = false,
        val currentFilters: List<OrderFilterUi> = emptyList(),
        val filters: List<OrderFilterUi> = emptyList(),
        override val items: List<OrdersHistoryItemUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val showRefreshIndicator: Boolean = false,
    ) : PagingState<OrdersHistoryItemUi, AllOrdersState>() {

        override fun copyPagingState(
            items: List<OrdersHistoryItemUi>,
            loadStates: CombinedLoadStates,
        ): AllOrdersState = copy(items = items, loadStates = loadStates)

    }

    sealed class AllOrdersEvent : Event {
        data object GoToCart : AllOrdersEvent()
        data object GoBack : AllOrdersEvent()
        data object GoToCatalog : AllOrdersEvent()

        data class GoToOrderDetails(val id: Long) : AllOrdersEvent()
        data class OpenUrl(val url: String) : AllOrdersEvent()
        data class GoToWebView(val url: String) : AllOrdersEvent()
    }

    @Stable
    sealed interface AllOrdersUiState {
        data object Loading : AllOrdersUiState
        data object Error : AllOrdersUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AllOrdersUiState
        data object Body : AllOrdersUiState
    }
}