package com.m.vodovoz.feature.order_recipient

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.EmailValidator
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.INNValidator
import com.m.vodovoz.design_system.model.widgets.NameValidator
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.PhoneNumberValidator
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.design_system.model.widgets.mapToDomain
import com.m.vodovoz.design_system.model.widgets.mapToUi
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.order_recipient.model.OrderRecipientEvent
import com.m.vodovoz.feature.order_recipient.model.OrderRecipientState
import com.m.vodovoz.feature.order_recipient.model.OrderRecipientUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderRecipientViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<OrderRecipientState, OrderRecipientEvent>(OrderRecipientState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: -1

    init {
        fetchOrderRecipientDetails()
    }


    fun fetchOrderRecipientDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = OrderRecipientUiState.Loading)
        }

        val recipientDetailsResult =
            vodovozServiceRepository.getOrderRecipientDetails(addressId).singleResult()

        recipientDetailsResult.onSuccess { recipientDetails ->
            updateState { s ->
                s.copy(
                    uiState = OrderRecipientUiState.Recipient,
                    title = recipientDetails.title,
                    fields = recipientDetails.fields.mapToUi(),
                    button = recipientDetails.button.toUi()
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(
                    uiState = OrderRecipientUiState.Error
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(OrderRecipientEvent.GoBack)
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) {
        val updatedFields = stateSnapshot.fields.updateField(
            field = field,
            newField = updatedField
        )

        val isValid = updatedFields.checkFields(
            putErrors = false,
            validators = listOf(
                PhoneNumberValidator,
            )
        )

        updateState { s ->
            s.copy(
                fields = updatedFields,
                button = s.button.copy(enabled = isValid)
            )
        }


    }

    fun activateButton() = viewModelScope.launch {
        stateSnapshot.fields.checkFields(
            putErrors = true,
            validators = listOf(
                NoRequiredValidator,
                INNValidator,
                PhoneNumberValidator,
                EmailValidator,
                NameValidator
            ),
            getSupportingText = { field ->
                field.getErrorText { resId -> resourcesProvider.getString(resId) }
            }
        ) { fields, isValid ->

            updateState { s ->
                s.copy(
                    button = s.button.copy(enabled = false),
                    fields = fields
                )
            }

            if (!isValid) {
                return@launch
            }
        }

        updateState { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        vodovozServiceRepository.sendOrderRecipient(
            addressId = addressId,
            fields = stateSnapshot.fields.mapToDomain()
        ).singleResult()

        sendEvent(OrderRecipientEvent.GoBackToOrdering)


        updateState { s ->
            s.copy(button = s.button.copy(loading = false))
        }

    }


}