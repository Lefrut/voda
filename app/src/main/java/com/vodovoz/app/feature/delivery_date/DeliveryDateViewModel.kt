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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        val deliveryDateDetailsResult = vodovozServiceRepository.getDeliveryDateDetails(
            addressId = addressId
        ).singleResult()

        deliveryDateDetailsResult.onSuccess { deliveryDateDetails ->

            _state.update { s ->
                s.copy(
                    title = deliveryDateDetails.title,
                    button = deliveryDateDetails.button.toUi(),
                    options = deliveryDateDetails.options.mapToUi(),
                    timeSections = deliveryDateDetails.timeSections.map { timeSection ->
                        timeSection.toUi { deliveryTime ->
                            deliveryTime.mapToUi()
                        }
                    },
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
            s.copy(selectedOption = dateOption)
        }
    }

    fun selectTimeSection(timeSection: SectionUi<DeliveryTimeIntervalUi>) {
        _state.update { s ->
            s.copy(selectedTimeSection = timeSection)
        }
    }

}