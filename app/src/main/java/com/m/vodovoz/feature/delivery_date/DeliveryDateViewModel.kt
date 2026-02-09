package com.m.vodovoz.feature.delivery_date

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.core.navigation.getQueryParams
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateEvent
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateOptionUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateState
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateUiState
import com.m.vodovoz.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.m.vodovoz.feature.delivery_date.model.mapToUi
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@Stable
class DeliveryDateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<DeliveryDateState, DeliveryDateEvent>(DeliveryDateState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: -1

    private val deliveryDate = savedStateHandle.get<String>("date")

    private val timeInterval = savedStateHandle.get<String>("timeInterval")

    private val earlierDelivery = savedStateHandle.get<Boolean>("earlierDelivery")

    private val queryParams = savedStateHandle.getQueryParams()

    init {
        if (deliveryDate != null && timeInterval != null) {
            updateState { s ->
                s.copy(
                    showCalendarDialog = false,
                    selectedDateOption = DeliveryDateOptionUi.Empty.copy(value = deliveryDate),
                    selectedTimeInterval = DeliveryTimeIntervalUi.Empty.copy(value = timeInterval),
                )
            }
        }
        fetchDeliveryDateDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(DeliveryDateEvent.GoBack)
    }

    fun fetchDeliveryDateDetails() = viewModelScope.launch {
        val selectedLocalDate = try {
            LocalDate.parse(stateSnapshot.selectedDateOption.value, VodovozDateFormatters.DMY)
        } catch (_: Throwable) {
            LocalDate.now().plusDays(1)
        }

        val deliveryDateDetailsResult = vodovozServiceRepository.getDeliveryDateDetails(
            addressId = addressId,
            date = selectedLocalDate,
            queryParams = queryParams
        ).singleResult()

        deliveryDateDetailsResult.onSuccess { deliveryDateDetails ->
            val dateOptions = deliveryDateDetails.options.mapToUi()
            val timeSections = deliveryDateDetails.timeSections.map { timeSection ->
                timeSection.toUi { deliveryTime -> deliveryTime.mapToUi() }
            }

            updateState { s ->
                val timeSectionBySelectedTimeInterval = timeSections.find { section ->
                    section.items.find { it.value == s.selectedTimeInterval.value } != null
                }


                val selectedTimeSection = timeSectionBySelectedTimeInterval
                    ?: timeSections.firstOrNull()
                    ?: SectionUi.empty()


                val selectedTimeInterval = selectedTimeSection.items.firstOrNull {
                    it.value == s.selectedTimeInterval.value
                } ?: DeliveryTimeIntervalUi.Empty

                s.copy(
                    title = deliveryDateDetails.title,
                    button = deliveryDateDetails.button.toUi().copy(
                        enabled = selectedTimeInterval != DeliveryTimeIntervalUi.Empty
                    ),
                    options = dateOptions,
                    timeSections = timeSections,
                    selectedTimeSection = selectedTimeSection,
                    selectedDateOption = s.selectedDateOption.takeIf {
                        it != DeliveryDateOptionUi.Empty
                    } ?: dateOptions.firstOrNull() ?: DeliveryDateOptionUi.Empty,
                    selectedTimeInterval = selectedTimeInterval,
                    uiState = DeliveryDateUiState.Success,
                    earlierCheckbox = deliveryDateDetails.earlierCheckbox?.toUi()?.copy(
                        checked = s.earlierCheckbox?.checked ?: (earlierDelivery == true)
                    )
                )
            }


        }.onFailure {
            updateState { s ->
                s.copy(uiState = DeliveryDateUiState.Error)
            }
        }
    }

    fun selectDateOption(dateOption: DeliveryDateOptionUi) {
        updateState { s ->
            s.copy(
                selectedDateOption = dateOption,
                uiState = DeliveryDateUiState.BodyLoading
            )
        }
        fetchDeliveryDateDetails()
    }

    fun selectTimeSection(timeSection: SectionUi<DeliveryTimeIntervalUi>) {


        updateState { s ->
            s.copy(
                selectedTimeSection = timeSection,
                selectedTimeInterval = DeliveryTimeIntervalUi.Empty,
                button = s.button.copy(enabled = false)
            )
        }
    }

    fun selectDeliveryTimeInterval(deliveryTimeInterval: DeliveryTimeIntervalUi) {
        updateState { s ->
            s.copy(
                selectedTimeInterval = deliveryTimeInterval,
                button = s.button.copy(
                    enabled = deliveryTimeInterval != DeliveryTimeIntervalUi.Empty
                )
            )
        }
    }

    fun chooseDeliveryDate() = viewModelScope.launch {
        val dateOption = stateSnapshot.selectedDateOption
        val timeInterval = stateSnapshot.selectedTimeInterval
        val earlierCheckbox = stateSnapshot.earlierCheckbox

        sendEvent(
            DeliveryDateEvent.GoBackToOrdering(
                timeInterval = timeInterval,
                dateOption = dateOption,
                earlierCheckbox = earlierCheckbox
            )
        )
    }

    fun showCalendarDialog() {
        updateState { s ->
            s.copy(showCalendarDialog = true)
        }
    }

    fun hideCalendarDialog() {
        updateState { s ->
            s.copy(showCalendarDialog = false)
        }
        if (stateSnapshot.uiState == DeliveryDateUiState.Loading) {
            navigateBack()
        }
    }

    fun selectCalendarDate(date: LocalDate) {
        val formattedDate = date.format(VodovozDateFormatters.DMY)

        if (formattedDate == stateSnapshot.selectedDateOption.value) {
            updateState { s ->
                s.copy(showCalendarDialog = false)
            }
            return
        }

        updateState { s ->
            s.copy(
                selectedDateOption = s.options.find { it.value == formattedDate }
                    ?: s.selectedDateOption.copy(
                        name = UUID.randomUUID().toString(),
                        value = formattedDate
                    ),
                uiState = if (stateSnapshot.uiState != DeliveryDateUiState.Loading) DeliveryDateUiState.BodyLoading else s.uiState,
                showCalendarDialog = false
            )
        }

        val uiState = stateSnapshot.uiState

        if (uiState == DeliveryDateUiState.Loading || uiState == DeliveryDateUiState.BodyLoading) {
            fetchDeliveryDateDetails()
        }
    }

    fun changeCheckbox(newEarlierCheckbox: CheckboxUi) {
        updateState { s -> s.copy(earlierCheckbox = newEarlierCheckbox) }
    }

}