package com.m.vodovoz.feature.cancel_order

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.cancel_order.api.CancelOrderNavKey
import com.m.vodovoz.feature.cancel_order.model.CancelOrderEvent
import com.m.vodovoz.feature.cancel_order.model.CancelOrderState
import com.m.vodovoz.feature.cancel_order.model.CancelOrderUiState
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CancelOrderViewModel.Factory::class)
@Stable
class CancelOrderViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    @Assisted private val navKey: CancelOrderNavKey,
) : MviViewModel<CancelOrderState, CancelOrderEvent>(CancelOrderState()) {

    private val orderId = navKey.orderId

    init {
        viewModelScope.launch { delay(250L) }.invokeOnCompletion {
            fetchCancelOrderDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(CancelOrderEvent.GoBack)
    }

    fun fetchCancelOrderDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = CancelOrderUiState.Loading)
        }

        val cancelOrderDetailsResult =
            vodovozServiceRepository.getCancelOrderDetails(orderId).singleResult()

        cancelOrderDetailsResult.onSuccess { cancelOrderDetails ->
            val checkboxes = cancelOrderDetails.checkboxesNames

            updateState { s ->
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
            updateState { s ->
                s.copy(uiState = CancelOrderUiState.Error)
            }
            navigateBack()
        }
    }

    fun changeCurrentCheckbox(name: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(currentCheckboxName = name)
        }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {

        val newField = listOf(field).updateFieldAndResetError(field, updatedField).firstOrNull()
            ?: return@launch

        updateState { s ->
            s.copy(commentField = newField)
        }
    }

    fun cancelOrder(btn: ColorfulButtonUi) = viewModelScope.launch {
        updateState { s ->
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
            sendEvent(CancelOrderEvent.GoBack)
        }

        updateState { s ->
            s.copy(button = s.button.copy(loading = false))
        }

    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: CancelOrderNavKey): CancelOrderViewModel
    }
}
