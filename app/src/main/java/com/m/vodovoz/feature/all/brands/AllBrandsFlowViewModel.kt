package com.m.vodovoz.feature.all.brands

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.design_system.model.BrandUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AllBrandsFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<AllBrandsFlowViewModel.AllBrandsState, AllBrandsFlowViewModel.AllBrandsEvents>(
    AllBrandsState()
) {

    init {
        fetchBrands()
    }

    private fun fetchBrands() = viewModelScope.launch {

        val brandsFlow =
            vodovozServiceRepository.getBrandsPaged(stateSnapshot.searchQuery).map { pagingData ->
                pagingData.map { brand -> brand.toUi() }
            }


        vodovozServiceRepository.getBrands(stateSnapshot.searchQuery).singleResult()
            .onSuccess { brandSectionModel ->
                updateState { s ->
                    s.copy(
                        uiState = AllBrandsUiState.Success,
                        title = brandSectionModel.title,
                        brands = brandsFlow
                    )
                }
            }.onFailure {
                navigateBack()
            }

    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AllBrandsEvents.GoBack)
    }


    fun changeSearchMode(searchMode: Boolean) = viewModelScope.launch {
        if (!searchMode) searchQueriesStateFlow.value = ""
        updateState { s ->
            s.copy(isSearchMode = searchMode)
        }
    }

    @OptIn(FlowPreview::class)
    private val searchQueriesStateFlow = MutableStateFlow("").apply {
        drop(1).onEach { newSearchQuery ->
            updateState { s -> s.copy(searchQuery = newSearchQuery) }
        }.debounce(200).onEach { _ ->
            fetchBrands()
        }.launchIn(viewModelScope)
    }

    fun changeSearchQuery(newSearchQuery: String) = viewModelScope.launch {
        searchQueriesStateFlow.value = newSearchQuery
    }

    fun navigateToBrandProducts(brandId: Long) = viewModelScope.launch {
        sendEvent(AllBrandsEvents.GoToBrandProducts(brandId))
    }

    @Immutable
    data class AllBrandsState(
        val brands: Flow<PagingData<BrandUi>> = emptyFlow(),
        val title: String = "",
        val searchQuery: String = "",
        val uiState: AllBrandsUiState = AllBrandsUiState.Loading,
        val isSearchMode: Boolean = false,
    ) : State

    sealed class AllBrandsEvents : Event {
        data class GoToBrandProducts(val brandId: Long) : AllBrandsEvents()
        data object GoBack : AllBrandsEvents()
    }

    @Stable
    sealed interface AllBrandsUiState {
        data object Loading : AllBrandsUiState
        data object Success : AllBrandsUiState
    }
}