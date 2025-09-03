package com.vodovoz.app.feature.filter_values

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.design_system.model.filters.FilterUi
import com.vodovoz.app.design_system.model.filters.FilterValueUi
import com.vodovoz.app.design_system.model.filters.mapToUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterValuesViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<FilterValuesViewModel.ConcreteFilterState, FilterValuesViewModel.ConcreteFilterEvent>(
    ConcreteFilterState()
) {

    private val filter = savedStateHandle.get<FilterUi>("filter") ?: FilterUi.Empty
    private val categoryId = savedStateHandle.get<Long>("categoryId")?.toInt() ?: -1

    init {
        viewModelScope.launch {
            delay(250L)
        }.invokeOnCompletion { fetchFilterValues() }
    }


    private fun fetchFilterValues() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = ConcreteFilterUiState.Loading)
        }
        val filterValuesResult =
            vodovozServiceRepository.getFilterValues(categoryId, filter.id).singleResult()

        filterValuesResult.onSuccess { filterValues ->

            updateState { s ->
                s.copy(
                    filter = filter.copy(
                        values = filterValues.mapToUi().map { value ->
                            value.copy(selected = filter.values.firstOrNull { it -> it.id == value.id && it.selected } != null)
                        }
                    ),
                    uiState = ConcreteFilterUiState.Success

                )
            }
        }.onFailure {
            navigateBack()
        }
    }

    fun selectFilterValue(filterValue: FilterValueUi) = viewModelScope.launch {
        updateState { s ->
            val filter = s.filter
            s.copy(
                filter = filter.copy(
                    values = filter.values.toMutableList().apply {
                        set(
                            indexOf(filterValue),
                            filterValue.copy(selected = !filterValue.selected)
                        )
                    }
                ),
                showApplyButton = true
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ConcreteFilterEvent.GoBack)
    }

    fun navigateToProductFilters() = viewModelScope.launch {
        val currentFilter = stateSnapshot.filter

        sendEvent(
            ConcreteFilterEvent.GoToProductFilters(
                filter = currentFilter.copy(
                    values = currentFilter.values
                )
            )
        )
    }

    fun changeSearchQuery(newSearchQuery: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(searchQuery = newSearchQuery)
        }
    }

    fun changeSearchMode(searchMode: Boolean) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                isSearchMode = searchMode,
                searchQuery = ""
            )
        }
    }

    @Immutable
    data class ConcreteFilterState(
        val filter: FilterUi = FilterUi.Empty,
        val searchQuery: String = "",
        val uiState: ConcreteFilterUiState = ConcreteFilterUiState.Loading,
        val showApplyButton: Boolean = false,
        val isSearchMode: Boolean = false
    ) : State {
    }

    @Immutable
    sealed interface ConcreteFilterUiState {
        data object Loading : ConcreteFilterUiState
        data object Success : ConcreteFilterUiState
    }

    sealed interface ConcreteFilterEvent : Event {
        data object GoBack : ConcreteFilterEvent
        data class GoToProductFilters(val filter: FilterUi) : ConcreteFilterEvent
    }

}