package com.vodovoz.app.feature.product_filters

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.design_system.model.filters.FilterUi
import com.vodovoz.app.design_system.model.filters.FilterValueUi
import com.vodovoz.app.design_system.model.filters.FiltersPriceUi
import com.vodovoz.app.design_system.model.filters.FiltersUi
import com.vodovoz.app.design_system.model.filters.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import com.vodovoz.app.util.roundToOneDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@HiltViewModel
class ProductFiltersFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<ProductFiltersFlowViewModel.ProductFiltersState, ProductFiltersFlowViewModel.ProductFiltersEvent>(
    ProductFiltersState()
) {

    private val categoryIdArg =
        savedStateHandle.remove<Long>("categoryId")?.toInt() ?: navigateBack().let { -1 }
    private val filtersArg = savedStateHandle.remove<FiltersUi>("filters") ?: FiltersUi.Empty

    init {
        listenFiltersSelectionState()
        fetchFiltersByCategory()
    }

    private fun listenFiltersSelectionState() = viewModelScope.launch {
        _state.map { stateSnapshot.filters }.collectLatest { filters ->
            val anyFilterHasSelected = filters.filters.any { filter ->
                filter.values.any { it.selected } || filter.bounds != filter.currentBounds
            }
            val filtersPrice = filters.price

            _state.update { s ->
                s.copy(
                    showClearButton = anyFilterHasSelected || filtersPrice.currentMin != filtersPrice.min || filtersPrice.currentMax != filtersPrice.max
                )
            }
        }
    }

    fun clearFilters() = viewModelScope.launch {
        _state.update { s ->
            val filters = s.filters
            val price = filters.price

            s.copy(
                filters = filters.copy(
                    filters = filters.filters.map { filter ->
                        filter.copy(
                            values = filter.values.map { it.copy(selected = false) },
                            currentBounds = filter.bounds
                        )
                    },
                    price = filters.price.copy(
                        currentMax = price.max,
                        currentMin = price.min
                    )
                ),
            )
        }
        sendEvent(ProductFiltersEvent.ResetSlider)
    }


    fun fetchFiltersByCategory() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = ProductFiltersUiState.Loading)
        }

        val filtersResult = vodovozServiceRepository.getFilters(categoryIdArg).singleResult()
        filtersResult.onSuccess { filters ->
            _state.update { s ->
                s.copy(
                    filters = mergeFilters(filtersArg, filters.toUi()),
                    uiState = ProductFiltersUiState.Success
                )
            }
        }.onFailure { navigateBack() }
    }


    fun navigateBack() = viewModelScope.launch {
        sendEvent(ProductFiltersEvent.GoBack)
    }

    fun changePriceFromField(min: String) = viewModelScope.launch {
        _state.update { s ->
            val price = s.filters.price
            s.copy(
                filters = s.filters.copy(
                    price = price.copy(
                        currentMin = min.toFloatOrNull()?.toInt()
                            ?.takeIf { it != Int.MAX_VALUE }
                            ?: s.filters.price.min
                    )
                )
            )
        }
    }

    fun changePriceToField(max: String) = viewModelScope.launch {
        _state.update { s ->
            val price = s.filters.price
            s.copy(
                filters = s.filters.copy(
                    price = price.copy(
                        currentMax = max.toFloatOrNull()?.toInt()
                            ?.takeIf { it != Int.MAX_VALUE }
                            ?: s.filters.price.max
                    )
                )
            )
        }
    }


    fun changeFiltersPrice(range: ClosedFloatingPointRange<Float>) = viewModelScope.launch {
        _state.update { s ->

            val filtersPrice = s.filters.price
            val delta = filtersPrice.max - filtersPrice.min
            s.copy(
                filters = s.filters.copy(
                    price = filtersPrice.copy(
                        currentMin = filtersPrice.min + (delta * range.start).roundToInt(),
                        currentMax = filtersPrice.min + (delta * range.endInclusive).roundToInt()
                    )
                ),
                showApplyButton = true,
            )
        }
    }


    fun selectFilterValue(filter: FilterUi, filterValue: FilterValueUi) = viewModelScope.launch {
        _state.update { s ->
            val filters = s.filters
            s.copy(
                filters = filters.copy(
                    filters = s.filters.filters.map { currentFilter ->
                        if (currentFilter == filter) {
                            val updatedValues = currentFilter.values.mapNotNull { currentValue ->
                                if (currentValue == filterValue && currentValue.selected && currentFilter.values.size > 6) null
                                else if (currentValue == filterValue) currentValue.copy(selected = !currentValue.selected)
                                else currentValue
                            }

                            val sortedUpdatedValues = try {
                                updatedValues.sortedWith(filterValueComparator)
                            } catch (_: Throwable) {
                                updatedValues
                            }

                            currentFilter.copy(values = sortedUpdatedValues)
                        } else currentFilter
                    }
                ),
                showApplyButton = true
            )
        }
    }

    fun navigateToFilterValues(filter: FilterUi) = viewModelScope.launch {
        sendEvent(ProductFiltersEvent.GoToFilterValues(filter, categoryIdArg.toLong()))
    }

    fun navigateToProductList() = viewModelScope.launch {
        _state.update { s ->
            val price = s.filters.price
            s.copy(
                filters = s.filters.copy(
                    price = price.copy(
                        currentMin = price.currentMin.coerceIn(price.min, price.max),
                        currentMax = price.currentMax.coerceIn(price.min, price.max)
                    )
                )
            )
        }
        sendEvent(ProductFiltersEvent.GoToProductList(stateSnapshot.filters))
    }


    fun changeFilter(newFilter: FilterUi) = viewModelScope.launch {
        _state.update { s ->
            val filters = s.filters
            s.copy(
                filters = filters.copy(
                    filters = mergeFiltersWithPriority(
                        originalFilters = filters.filters,
                        priorityFilters = listOf(newFilter)
                    )
                ),
                showApplyButton = true
            )
        }
    }

    private val filterValueComparator: Comparator<FilterValueUi> =
        compareByDescending<FilterValueUi> { it -> it.selected }.thenBy { filterValue ->
            if (!filterValue.selected) {
                filterValue.name.replaceFirst(',', '.').toDoubleOrNull()
                    ?: filterValue.name
            } else ""
        }


    private fun mergeFilters(
        userFilters: FiltersUi,
        baseFilters: FiltersUi,
    ): FiltersUi {
        val basePrice = baseFilters.price
        val userPrice = userFilters.price

        val newMin = min(basePrice.min, basePrice.max)
        val newMax = max(basePrice.min, basePrice.max)

        val newCurrentMin =
            if (userPrice.currentMin in newMin..newMax) userPrice.currentMin else basePrice.currentMin
        val newCurrentMax =
            if (userPrice.currentMax in newMin..newMax) userPrice.currentMax else basePrice.currentMax

        return userFilters.copy(
            filters = mergeFilters(userFilters.filters, baseFilters.filters),
            price = FiltersPriceUi(
                min = newMin,
                max = newMax.coerceAtLeast(newMin),
                currentMin = newCurrentMin,
                currentMax = newCurrentMax.coerceAtLeast(newCurrentMin)
            )
        )
    }

    private fun mergeFilters(
        originalFilters: List<FilterUi>,
        newFilters: List<FilterUi>,
    ): List<FilterUi> {
        val allFilters = originalFilters + newFilters
        return allFilters.groupBy { filter -> filter.id }.mapNotNull { idAndFilters ->
            val currentFilter = idAndFilters.value.firstOrNull() ?: return@mapNotNull null

            val filterValues = idAndFilters.value.map { it -> it.values }.flatten()

            val sortedFilterValues = try {
                filterValues.sortedWith(filterValueComparator)
            } catch (_: Throwable) {
                filterValues
            }.distinctBy { it -> it.id }

            currentFilter.copy(
                values = sortedFilterValues.filterIndexed { index, filterValue ->
                    filterValue.selected || index < 6
                }

            )
        }
    }

    private fun mergeFiltersWithPriority(
        originalFilters: List<FilterUi>,
        priorityFilters: List<FilterUi>,
    ): List<FilterUi> {
        val allFilters = originalFilters + priorityFilters
        return allFilters.groupBy { filter -> filter.id }.mapNotNull { idAndFilters ->
            val currentFilter = idAndFilters.value.firstOrNull() ?: return@mapNotNull null
            val filterValues = idAndFilters.value
                .asSequence()
                .map { it.values }
                .flatten().groupBy { it.id }
                .mapNotNull { idAndValues ->
                    val values = idAndValues.value.distinctBy { it.selected }
                    values.lastOrNull()
                }.sortedWith(compareByDescending { it.selected })
                .toList()


            currentFilter.copy(
                values = filterValues.filterIndexed { index, filterValue ->
                    filterValue.selected || index < 6
                }

            )
        }
    }

    fun changeFilterRange(filter: FilterUi, range: ClosedFloatingPointRange<Float>) =
        viewModelScope.launch {
            _state.update { s ->

                val target = s.filters.filters.find { it.id == filter.id } ?: return@update s
                val bounds = target.bounds ?: return@update s

                val delta = bounds.endInclusive - bounds.start

                val newMin = (bounds.start + delta * range.start).roundToOneDecimal()
                val newMax = (bounds.start + delta * range.endInclusive).roundToOneDecimal()

                val updatedBounds = target.copy(
                    currentBounds = newMin..newMax
                )

                s.copy(
                    filters = s.filters.copy(
                        filters = s.filters.filters.map {
                            if (it.id == filter.id) updatedBounds else it
                        }
                    )
                )
            }
        }

    fun changeFilterFrom(filter: FilterUi, value: Float) {
        _state.update { s ->

            val target = s.filters.filters.find { it.id == filter.id } ?: return@update s
            val bounds = target.bounds ?: return@update s
            val current = target.currentBounds ?: bounds

            val updatedBounds =
                target.copy(currentBounds = min(current.endInclusive, value)..current.endInclusive)

            s.copy(
                filters = s.filters.copy(
                    filters = s.filters.filters.map {
                        if (it.id == filter.id) updatedBounds else it
                    }
                )
            )
        }
    }

    fun changeFilterTo(filter: FilterUi, value: Float) {
        _state.update { s ->

            val target = s.filters.filters.find { it.id == filter.id } ?: return@update s
            val bounds = target.bounds ?: return@update s
            val current = target.currentBounds ?: bounds

            val updatedBounds =
                target.copy(currentBounds = current.start..max(current.start, value))

            s.copy(
                filters = s.filters.copy(
                    filters = s.filters.filters.map {
                        if (it.id == filter.id) updatedBounds else it
                    }
                )
            )
        }

    }


    @Immutable
    data class ProductFiltersState(
        val filters: FiltersUi = FiltersUi.Empty,
        val uiState: ProductFiltersUiState = ProductFiltersUiState.Loading,
        val showApplyButton: Boolean = false,
        val showClearButton: Boolean = false,
    ) : State

    @Stable
    sealed interface ProductFiltersUiState {
        data object Loading : ProductFiltersUiState
        data object Success : ProductFiltersUiState
        data object Error : ProductFiltersUiState
    }

    @Immutable
    sealed interface ProductFiltersEvent : Event {
        data object GoBack : ProductFiltersEvent
        data class GoToFilterValues(val filter: FilterUi, val categoryId: Long) :
            ProductFiltersEvent

        data class GoToProductList(val filters: FiltersUi) :
            ProductFiltersEvent

        data object ResetSlider : ProductFiltersEvent
    }
}