package com.vodovoz.app.feature.preorder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.toDomain
import com.vodovoz.app.design_system.model.widgets.vodovozValidators
import com.vodovoz.app.domain.general.model.ValidationException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.PreOrderSectionUi
import com.vodovoz.app.feature.preorder.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreOrderFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<PreOrderFlowViewModel.PreOrderState, PreOrderFlowViewModel.PreOrderEvent>(
    PreOrderState()
) {

    private val productId = savedState.get<Long>("productId") ?: -1L

    fun fetchPreOrderData() = viewModelScope.launch {
        vodovozServiceRepository.getPreorderFields(productId)
            .onStart { uiStateListener.updateData { s -> s.copy(uiState = UiState.Loading) } }
            .onEach { preOrderSectionResult ->
                preOrderSectionResult.onSuccess { preOrderSectionModel ->
                    uiStateListener.updateData { s ->
                        s.copy(
                            uiState = UiState.Success,
                            sectionPreOrder = preOrderSectionModel.toUi(),
                        )
                    }
                }.onFailure {
                    uiStateListener.updateData { s ->
                        s.copy(uiState = UiState.Error)
                    }
                }
            }.collect()
    }

    fun sendPreOrder() = viewModelScope.launch {
        if (!validateFields()) return@launch

        val fields = dataState.sectionPreOrder.fields.map { field -> field.toDomain() }

        vodovozServiceRepository.sendPreorder(productId, fields).take(1).collect { result ->
            result.onSuccess { message ->
                eventListener.emit(PreOrderEvent.ShowSnackbar(message, true))
                eventListener.emit(PreOrderEvent.GoBack)
            }.onFailure { t ->
                val errorMessage = when (t) {
                    is ValidationException -> {
                        t.message
                            ?: resourcesProvider.getString(R.string.error_message_send_failed)
                    }

                    else -> {
                        resourcesProvider.getString(R.string.error_message_send_failed)
                    }
                }
                eventListener.emit(PreOrderEvent.ShowSnackbar(errorMessage))
            }
        }

        eventListener.emit(PreOrderEvent.HideKeyboard)
    }


    private fun validateFields(): Boolean {

        val fields = dataState.sectionPreOrder.fields

        return fields.checkFields(
            putErrors = true,
            validators = vodovozValidators,
            getSupportingText = { f ->
                f.getErrorText { id -> resourcesProvider.getString(id) }
            }
        ) { updatedFields, _ ->
            uiStateListener.updateData { s ->
                s.copy(
                    sectionPreOrder = s.sectionPreOrder.copy(
                        fields = updatedFields
                    )
                )
            }
        }
    }


    fun changeFieldValue(field: FieldUi, newValue: String) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            val sectionPreOrder = s.sectionPreOrder
            val fields = sectionPreOrder.fields
            val fieldIndex = fields.indexOfFirst { field.id == it.id }

            s.copy(
                sectionPreOrder = sectionPreOrder.copy(
                    fields = fields.toMutableList()
                        .apply { set(fieldIndex, field.copy(value = newValue)) }
                        .map { it.copy(isError = false) }
                )
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(PreOrderEvent.GoBack)
    }

    data class PreOrderState(
        val sectionPreOrder: PreOrderSectionUi = PreOrderSectionUi.Empty,
        val uiState: UiState = UiState.Loading,
    ) : State

    sealed class PreOrderEvent : Event {
        data class ShowSnackbar(val message: String, val isVeryShort: Boolean = false) :
            PreOrderEvent()

        data object GoBack : PreOrderEvent()
        data object HideKeyboard : PreOrderEvent()
    }

    sealed interface UiState {
        data object Error : UiState
        data object Success : UiState
        data object Loading : UiState
    }
}