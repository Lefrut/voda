package com.m.vodovoz.feature.filter_values

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.m.vodovoz.design_system.model.filters.FilterUi
import com.m.vodovoz.design_system.model.filters.FilterValueUi
import com.m.vodovoz.design_system.model.filters.mapToUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.filter_values.api.FilterValuesNavKey
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FilterValuesViewModel.Factory::class)
class FilterValuesViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    @Assisted private val navKey: FilterValuesNavKey?,
) : MviViewModel<FilterValuesViewModel.ConcreteFilterState, FilterValuesViewModel.ConcreteFilterEvent>(
    ConcreteFilterState()
) {

    private val filter = navKey?.filter ?: FilterUi.Empty
    private val categoryId = navKey?.categoryId?.toInt() ?: -1

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

    @AssistedFactory
    interface Factory {
        fun create(navKey: FilterValuesNavKey?): FilterValuesViewModel
    }

}
