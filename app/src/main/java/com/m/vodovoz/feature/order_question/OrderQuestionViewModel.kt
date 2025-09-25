package com.m.vodovoz.feature.order_question

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.order_question.model.OrderQuestionEvent
import com.m.vodovoz.feature.order_question.model.OrderQuestionState
import com.m.vodovoz.feature.order_question.model.OrderQuestionUiState
import com.m.vodovoz.design_system.model.widgets.EmptyTextValidator
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.PhoneNumberValidator
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.design_system.model.widgets.mapToDomain
import com.m.vodovoz.design_system.model.widgets.mapToUi
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.design_system.model.widgets.vodovozValidators
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderQuestionViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<OrderQuestionState, OrderQuestionEvent>(
    OrderQuestionState()
) {
    private val orderId = savedStateHandle.get<Long>("orderId") ?: 0L.also { navigateBack() }

    init {
        viewModelScope.launch { delay(350) }.invokeOnCompletion {
            fetchOrderQuestionDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(OrderQuestionEvent.GoBack)
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        updateState { s ->

            val updatedFields = s.fields.updateFieldAndResetError(field, updatedField)
            val buttonIsEnabled = updatedFields.checkFields(
                validators = listOf(PhoneNumberValidator, EmptyTextValidator)
            )

            s.copy(
                fields = updatedFields,
                button = s.button.copy(enabled = buttonIsEnabled)
            )
        }
    }

    fun sendMessage() = viewModelScope.launch {
        if (!stateSnapshot.button.enabled) return@launch

        val isValidFields = stateSnapshot.fields.checkFields(
            putErrors = true,
            validators = vodovozValidators,
            getSupportingText = { field ->
                field.getErrorText { id ->
                    resourcesProvider.getString(id)
                }
            }
        ) { fields, isValidFields ->
            updateState { s ->
                s.copy(
                    fields = fields,
                    button = s.button.copy(
                        enabled = isValidFields
                    )
                )
            }
        }

        if (!isValidFields) return@launch

        updateState { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val sendOrderQuestionResult =
            vodovozServiceRepository.sendOrderQuestion(orderId, stateSnapshot.fields.mapToDomain())
                .singleResult()

        sendOrderQuestionResult.onSuccess { placeholderData ->
            updateState { s ->
                s.copy(
                    uiState = OrderQuestionUiState.Success(placeholderData.toUi()),
                    button = s.button.copy(loading = false)
                )
            }
        }.onFailure { t ->
            sendEvent(
                OrderQuestionEvent.ShowToast(
                    resourcesProvider.getString(R.string.order_question_send_error)
                )
            )
            updateState { s ->
                s.copy(
                    button = s.button.copy(loading = false)
                )
            }
        }
    }

    fun fetchOrderQuestionDetails() = viewModelScope.launch {
        updateState { s -> s.copy(uiState = OrderQuestionUiState.Loading) }

        val orderQuestionDetailsResult =
            vodovozServiceRepository.getOrderQuestionDetails(orderId).singleResult()
        orderQuestionDetailsResult.onSuccess { orderQuestionDetails ->

            updateState { s ->
                s.copy(
                    title = orderQuestionDetails.title,
                    description = orderQuestionDetails.description,
                    fields = orderQuestionDetails.fields.mapToUi(),
                    uiState = OrderQuestionUiState.Fields,
                    button = orderQuestionDetails.button.toUi().copy(enabled = false)
                )
            }

        }.onFailure {
            updateState { s ->
                s.copy(uiState = OrderQuestionUiState.Error)
            }
        }
    }

}