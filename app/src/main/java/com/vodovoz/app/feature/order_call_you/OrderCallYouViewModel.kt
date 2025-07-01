package com.vodovoz.app.feature.order_call_you

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.order_call_you.model.CallYouItemUi
import com.vodovoz.app.feature.order_call_you.model.OrderCallYouEvent
import com.vodovoz.app.feature.order_call_you.model.OrderCallYouState
import com.vodovoz.app.feature.order_call_you.model.OrderCallYouUiState
import com.vodovoz.app.feature.order_call_you.model.mapToUi
import com.vodovoz.app.feature.order_call_you.model.toUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderCallYouViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<OrderCallYouState, OrderCallYouEvent>(OrderCallYouState()) {

    private val addressId: Long = savedStateHandle.get<Long>("addressId") ?: -1

    init {
        fetchOrderCallYouDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(OrderCallYouEvent.GoBack)
    }

    fun fetchOrderCallYouDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = OrderCallYouUiState.Loading)
        }

        val callYouDetailsResult =
            vodovozServiceRepository.getOrderCallYouDetails(addressId).singleResult()

        callYouDetailsResult.onSuccess { callYouDetails ->
            _state.update { s ->
                s.copy(
                    uiState = OrderCallYouUiState.CallYou,
                    title = callYouDetails.title,
                    currentItem = callYouDetails.currentItem.toUi(),
                    items = callYouDetails.items.mapToUi(),
                    button = callYouDetails.button.toUi()
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(
                    uiState = OrderCallYouUiState.Error
                )
            }

        }
    }

    fun chooseOrderingCallYou(button: ColorfulButtonUi) = viewModelScope.launch {
        _events.emit(OrderCallYouEvent.GoBackToOrdering(stateSnapshot.currentItem))
    }

    fun selectCallYouItem(item: CallYouItemUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(currentItem = item)
        }
    }
}