package com.vodovoz.app.feature.order_recipient

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateField
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.order_recipient.model.OrderRecipientEvent
import com.vodovoz.app.feature.order_recipient.model.OrderRecipientState
import com.vodovoz.app.feature.order_recipient.model.OrderRecipientUiState
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderRecipientViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<OrderRecipientState, OrderRecipientEvent>(OrderRecipientState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: -1

    init {
        fetchOrderRecipientDetails()
    }


    fun fetchOrderRecipientDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = OrderRecipientUiState.Loading)
        }

        val recipientDetailsResult =
            vodovozServiceRepository.getOrderRecipientDetails(addressId).singleResult()

        recipientDetailsResult.onSuccess { recipientDetails ->
            _state.update { s ->
                s.copy(
                    uiState = OrderRecipientUiState.Recipient,
                    title = recipientDetails.title,
                    fields = recipientDetails.fields.mapToUi(),
                    button = recipientDetails.button.toUi()
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(
                    uiState = OrderRecipientUiState.Error
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(OrderRecipientEvent.GoBack)
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) {
        _state.update { s ->
            s.copy(
                fields = s.fields.updateField(field, updatedField),
            )
        }
    }

    fun activateButton(button: ColorfulButtonUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        vodovozServiceRepository.sendOrderRecipient(
            addressId = addressId,
            fields = stateSnapshot.fields.mapToDomain()
        ).singleResult().onSuccess {
            _events.emit(OrderRecipientEvent.GoBack)
        }

        _state.update { s ->
            s.copy(button = s.button.copy(loading = false))
        }

    }


}