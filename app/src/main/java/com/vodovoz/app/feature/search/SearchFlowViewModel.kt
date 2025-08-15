package com.vodovoz.app.feature.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.search.SearchManager
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.paging.ItemsState
import com.vodovoz.app.ui.paging.ProductsMviViewModel
import com.vodovoz.app.util.extensions.debounceWithMax
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
@Stable
class SearchFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val searchManager: SearchManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ProductsMviViewModel<ProductUi, SearchFlowViewModel.SearchState, SearchFlowViewModel.SearchEvents>(
    state = SearchState(),
    blockedProductsFlow = cartManager.blockedProductsState,
    favoritesFlow = likeManager.observeLikes(),
    cartFlow = cartManager.observeCarts(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val previousSearchQuery: String = savedStateHandle.get<String>("query") ?: ""

    private val querySharedFlow = MutableSharedFlow<String>(10)

    init {
        handleQueries()
    }

    suspend fun listenSearchHistory() =
        _state.combine(searchManager.fetchSearchHistoryFlow()) { _, searchHistory ->
            searchHistory
        }.collectLatest { searchHistory ->
            _state.update { s ->
                s.copy(
                    searchHistory = searchHistory.filter { query ->
                        query.contains(s.query)
                    }
                )
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun handleQueries() =
        querySharedFlow.onStart {
            _state.update { s ->
                s.copy(
                    uiState = UiState.Loading,
                    query = previousSearchQuery
                )
            }
            emit(previousSearchQuery)
        }.debounceWithMax(200L, 5).mapLatest { query ->
            fun checkAvailableData() {
                if (stateSnapshot.matchingQueries.isEmpty() && stateSnapshot.sectionRecommendations.items.isEmpty()) {
                    _state.update { s ->
                        s.copy(uiState = UiState.Error)
                    }
                }
            }

            val timeout: Long =
                if (stateSnapshot.uiState == UiState.Loading) 30_000L else 3_000L

            withTimeoutOrNull(timeout) {
                if (query.isBlank()) {
                    searchByEmptyQuery()
                } else {
                    searchByQuery(query)
                }
            } ?: checkAvailableData()
        }.launchIn(viewModelScope)


    private suspend fun searchByQuery(query: String) {
        val miniSearchRecommendationsResult =
            vodovozServiceRepository.getMiniSearchRecommendations(query).singleOrNull() ?: return
        miniSearchRecommendationsResult.onSuccess { miniSearchRecommendations ->
            val queries = miniSearchRecommendations.queries
            val section = miniSearchRecommendations.section.toUi()


            _state.update { s ->
                s.copy(
                    matchingQueries = queries,
                    sectionRecommendations = section,
                    uiState = UiState.Success
                )
            }


        }.onFailure { error ->

            val uiState = when (error) {
                is EmptyResultException -> with(error.placeholder) {
                    UiState.Empty(
                        description = this?.descriptionHtml ?: "",
                        image = this?.imageUrl ?: ""
                    )
                }

                else -> UiState.Error
            }

            if (uiState is UiState.Empty || (uiState is UiState.Error && stateSnapshot.sectionRecommendations.items.isEmpty())) {
                _state.update { s ->
                    s.copy(uiState = uiState)
                }
            }
        }
    }

    private suspend fun searchByEmptyQuery() {
        val searchRecommendationsResult =
            vodovozServiceRepository.getSearchRecommendations().singleOrNull() ?: return
        searchRecommendationsResult.onSuccess { searchRecommendations ->
            val queries = searchRecommendations.queries
            val section = searchRecommendations.section.toUi()

            _state.update { s ->
                s.copy(
                    matchingQueries = queries,
                    sectionRecommendations = section,
                    uiState = UiState.Success
                )
            }
        }.onFailure {
            val currentQuery = stateSnapshot.query
            if (stateSnapshot.matchingQueries.isEmpty() && stateSnapshot.sectionRecommendations.items.isEmpty() && currentQuery.isBlank()) {
                _state.update { s ->
                    s.copy(uiState = UiState.Error)
                }
            }
        }

    }

    fun retrySearchQuery() = viewModelScope.launch {
        _state.update { s -> s.copy(uiState = UiState.Loading) }
        val query = stateSnapshot.query
        if (query.isBlank()) {
            searchByEmptyQuery()
        } else {
            searchByQuery(query)
        }
    }

    fun search() = viewModelScope.launch {
        val currentQuery = stateSnapshot.query
        if (currentQuery.isBlank()) return@launch

        launch { searchManager.addQueryToHistory(currentQuery) }
        sendEvent(SearchEvents.GoToSearchProductList(currentQuery))
    }


    fun chooseMatchingQuery(query: String) = viewModelScope.launch {
        changeQuery(query).join()
        search()
    }

    fun changeQuery(query: String) = viewModelScope.launch {
        val currentQuery = stateSnapshot.query
        if (query == currentQuery) return@launch

        _state.update { s ->
            s.copy(query = query)
        }
        querySharedFlow.emit(query)
    }


    fun navigateToScan() = viewModelScope.launch {
        sendEvent(SearchEvents.GoToScanner)
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(SearchEvents.GoBack)
    }

    fun removeSearchQuery(searchQuery: String) = viewModelScope.launch {
        searchManager.removeQueryFromHistory(searchQuery)
    }

    fun changeFavorite(product: ProductUi) = viewModelScope.launch {
        likeManager.changeFavorite(product.id, !product.isFavorite)
    }

    fun navigateToProductDetails(product: ProductUi) = viewModelScope.launch {
        sendEvent(SearchEvents.GoToProductDetails(product.id))
    }

    fun navigateToProductAnalogs(product: ProductUi) = viewModelScope.launch {
        sendEvent(SearchEvents.GoToProductAnalogs(product.id))
    }

    fun incrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity + 1)
    }

    fun decrementProductToCart(product: ProductUi) = viewModelScope.launch {
        cartManager.change(product.id, product.cartQuantity - 1)
    }


    sealed class SearchEvents : Event {
        data class GoToPreOrder(val productId: Long, val name: String, val detailPicture: String) :
            SearchEvents()

        data object GoToProfile : SearchEvents()

        data class GoToWebView(val url: String, val title: String) : SearchEvents()

        data class GoToService(val id: String) : SearchEvents()
        data class GoToSearchProductList(val query: String) : SearchEvents()

        data class GoToProductDetails(val productId: Long) : SearchEvents()
        data class GoToProductAnalogs(val productId: Long) : SearchEvents()

        data object GoToContacts : SearchEvents()

        data object GoToPromotions : SearchEvents()
        data object GoBack : SearchEvents()
        data object GoToScanner : SearchEvents()
    }

    @Immutable
    data class SearchState(
        val query: String = "",
        val matchingQueries: List<String> = emptyList(),
        val uiState: UiState = UiState.Loading,
        val sectionRecommendations: SectionUi<ProductUi> = SectionUi.empty(),
        val searchHistory: List<String> = emptyList(),
    ) : ItemsState<ProductUi, SearchState>() {

        override val items: List<ProductUi>
            get() = sectionRecommendations.items

        override fun withItems(newItems: List<ProductUi>): SearchState {
            return copy(
                sectionRecommendations = sectionRecommendations.copy(
                    items = newItems
                )
            )
        }
    }

    @Stable
    sealed interface UiState {
        data object Loading : UiState
        data object Success : UiState
        data class Empty(val image: String, val description: String) : UiState
        data object Error : UiState

    }
}