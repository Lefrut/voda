package com.vodovoz.app.feature.cancel_order

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.cancel_order.model.CancelOrderEvent
import com.vodovoz.app.feature.cancel_order.model.CancelOrderState
import com.vodovoz.app.feature.cancel_order.model.CancelOrderUiState
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class CancelOrderViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<CancelOrderState, CancelOrderEvent>(CancelOrderState()) {

    private val orderId = savedStateHandle.get<Long>("orderId") ?: 0L.also { navigateBack() }

    init {
        viewModelScope.launch { delay(250L) }.invokeOnCompletion {
            fetchCancelOrderDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(CancelOrderEvent.GoBack)
    }

    fun fetchCancelOrderDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = CancelOrderUiState.Loading)
        }

        val cancelOrderDetailsResult =
            vodovozServiceRepository.getCancelOrderDetails(orderId).singleResult()

        cancelOrderDetailsResult.onSuccess { cancelOrderDetails ->
            val checkboxes = cancelOrderDetails.checkboxesNames

            _state.update { s ->
                s.copy(
                    uiState = CancelOrderUiState.Body,
                    title = cancelOrderDetails.title,
                    warningText = cancelOrderDetails.warningText,
                    description = cancelOrderDetails.description,
                    checkboxesGroupId = cancelOrderDetails.checkboxesGroupId,
                    checkboxesNames = checkboxes,
                    currentCheckboxName = checkboxes.firstOrNull() ?: "",
                    button = cancelOrderDetails.button.toUi(),
                    commentField = cancelOrderDetails.field?.toUi()
                )
            }

        }.onFailure {
            _state.update { s ->
                s.copy(uiState = CancelOrderUiState.Error)
            }
            navigateBack()
        }
    }

    fun changeCurrentCheckbox(name: String) = viewModelScope.launch {
        _state.update { s ->
            s.copy(currentCheckboxName = name)
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {

        val newField = listOf(field).updateFieldAndResetError(field, updatedField).firstOrNull()
            ?: return@launch

        _state.update { s ->
            s.copy(commentField = newField)
        }
    }

    fun cancelOrder(btn: ColorfulButtonUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val params = buildList {
            add(stateSnapshot.checkboxesGroupId to stateSnapshot.currentCheckboxName)
            stateSnapshot.commentField?.let {
                add(it.id to it.value)
            }
        }.associate { it.first to it.second }

        val cancelOrderResult = vodovozServiceRepository.cancelOrder(orderId, params).singleResult()

        cancelOrderResult.onSuccess {
            _events.emit(CancelOrderEvent.GoBack)
        }

        _state.update { s ->
            s.copy(button = s.button.copy(loading = false))
        }

    }

}