package com.vodovoz.app.feature.delivery_date

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateEvent
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateOptionUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateState
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateUiState
import com.vodovoz.app.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.vodovoz.app.feature.delivery_date.model.mapToUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
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

    private val addressId = savedStateHandle.get<Int>("addressId") ?: navigateBack().run { -1 }

    init {
        fetchDeliveryDateDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(DeliveryDateEvent.GoBack)
    }

    fun fetchDeliveryDateDetails() = viewModelScope.launch {

        //todo - need changes
        val selectedLocalDate = try {
            LocalDate.parse(stateSnapshot.selectedOption.value, VodovozDateFormatters.DMY)
        } catch (_: Throwable) {
            LocalDate.now().plusDays(1)
        }

        val deliveryDateDetailsResult = vodovozServiceRepository.getDeliveryDateDetails(
            addressId = addressId,
            date = selectedLocalDate
        ).singleResult()

        deliveryDateDetailsResult.onSuccess { deliveryDateDetails ->
            val options = deliveryDateDetails.options.mapToUi()
            val timeSections = deliveryDateDetails.timeSections.map { timeSection ->
                timeSection.toUi { deliveryTime -> deliveryTime.mapToUi() }
            }
            val firstSection = timeSections.firstOrNull() ?: SectionUi.empty()

            _state.update { s ->
                s.copy(
                    title = deliveryDateDetails.title,
                    button = deliveryDateDetails.button.toUi(),
                    options = options,
                    timeSections = timeSections,
                    selectedTimeSection = firstSection,
                    selectedOption = s.selectedOption.takeIf { it != DeliveryDateOptionUi.Empty }
                        ?: options.firstOrNull() ?: DeliveryDateOptionUi.Empty,
                    selectedTimeInterval = firstSection.items.firstOrNull()
                        ?: DeliveryTimeIntervalUi.Empty,
                    uiState = DeliveryDateUiState.Success
                )
            }


        }.onFailure {
            _state.update { s ->
                s.copy(
                    uiState = DeliveryDateUiState.Error
                )
            }
        }
    }

    fun selectDateOption(dateOption: DeliveryDateOptionUi) {
        _state.update { s ->
            s.copy(
                selectedOption = dateOption,
                uiState = DeliveryDateUiState.BodyLoading
            )
        }
        fetchDeliveryDateDetails()
    }

    fun selectTimeSection(timeSection: SectionUi<DeliveryTimeIntervalUi>) {
        _state.update { s ->
            s.copy(
                selectedTimeSection = timeSection,
                selectedTimeInterval = timeSection.items.firstOrNull()
                    ?: DeliveryTimeIntervalUi.Empty
            )
        }
    }

    fun selectDeliveryTimeInterval(deliveryTimeInterval: DeliveryTimeIntervalUi) {
        _state.update { s ->
            s.copy(
                selectedTimeInterval = deliveryTimeInterval
            )
        }
    }

    fun chooseDeliveryDate() {
        //todo - need finish
    }

    fun showCalendarDialog() {
        _state.update { s ->
            s.copy(showCalendarDialog = true)
        }
    }

    fun hideCalendarDialog() {
        _state.update { s ->
            s.copy(showCalendarDialog = false)
        }
    }

    fun selectCalendarDate(date: LocalDate) {
        val formattedDate = date.format(VodovozDateFormatters.DMY)

        if (formattedDate == stateSnapshot.selectedOption.value) return

        _state.update { s ->
            s.copy(
                selectedOption = s.options.find { it.value == formattedDate }
                    ?: s.selectedOption.copy(
                        name = UUID.randomUUID().toString(),
                        value = formattedDate
                    ),
                uiState = DeliveryDateUiState.BodyLoading
            )
        }
        fetchDeliveryDateDetails()
    }

}