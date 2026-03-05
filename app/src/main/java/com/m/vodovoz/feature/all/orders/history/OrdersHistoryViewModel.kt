package com.m.vodovoz.feature.all.orders.history

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.common.account.AccountManager
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.map
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.BannerUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.all.orders.history.model.OrderFilterUi
import com.m.vodovoz.feature.all.orders.history.model.OrdersHistoryItemUi
import com.m.vodovoz.feature.all.orders.history.model.mapToUi
import com.m.vodovoz.feature.all.orders.history.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingMviViewModel
import com.m.vodovoz.ui.paging.PagingState
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import com.m.vodovoz.util.extensions.debounceWithMax
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrdersHistoryViewModel @Inject constructor(
    val accountManager: AccountManager,
    val tabManager: TabManager,
    val cookieManager: CookieManager,
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
            updateState { s ->
                s.copy(uiState = AllOrdersUiState.Loading)
            }
        }

        val ordersHistoryDetailsResult =
            vodovozServiceRepository.getOrdersHistoryDetails().singleResult()

        ordersHistoryDetailsResult.onSuccess { ordersHistoryDetails ->


            updateState { s ->
                s.copy(
                    title = ordersHistoryDetails.title,
                    filters = ordersHistoryDetails.filters.mapToUi(),
                    banners = ordersHistoryDetails.banners.mapToUi(),
                    uiState = AllOrdersUiState.Body,
                    showRefreshIndicator = false
                )
            }

            vodovozServiceRepository.getOrdersHistoryItemsPaged(
                stateSnapshot.currentFilters.joinToString(",") { it.id },
                stateSnapshot.searchQuery
            ).map { pagingData ->
                pagingData.map { historyItemModel ->
                    historyItemModel.toUi()
                }
            }.collectPagingData()


        }.onFailure { t ->

            val uiState = when {
                t is EmptyResultException && t.placeholder != null -> AllOrdersUiState.Empty(t.placeholder.toUi())
                else -> AllOrdersUiState.Error
            }

            if (uiState is AllOrdersUiState.Body) return@onFailure

            updateState { s ->
                s.copy(uiState = uiState)
            }

        }
    }

    fun changeMode(searchMode: Boolean) = viewModelScope.launch {
        updateState { s ->
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
        updateState { s ->
            s.copy(searchQuery = query)
        }
        querySharedFlow.emit(query)
    }

    fun navigateToCatalog() = viewModelScope.launch {
        sendEvent(AllOrdersEvent.GoToCatalog)
    }

    fun selectAllFilters() = viewModelScope.launch {
        if (stateSnapshot.currentFilters.isEmpty()) return@launch

        updateState { s ->
            s.copy(currentFilters = emptyList())
        }

        fetchOrdersHistoryDetails()
    }

    fun selectFilter(filter: OrderFilterUi) = viewModelScope.launch {
        updateState { s ->
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

        fetchOrdersHistoryDetails().join()

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
        val currentFilters: List<OrderFilterUi> = emptyList(),
        val filters: List<OrderFilterUi> = emptyList(),
        val banners: List<BannerUi> = emptyList(),
        override val items: List<OrdersHistoryItemUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
        val showRefreshIndicator: Boolean = false,
        val aboutAdvertisingBS: AboutAdvertisingUi? = null,
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
        data class ActivateBanner(val banner: BannerUi) : AllOrdersEvent()
    }

    @Stable
    sealed interface AllOrdersUiState {
        data object Loading : AllOrdersUiState
        data object Error : AllOrdersUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AllOrdersUiState
        data object Body : AllOrdersUiState
    }
}
