package com.m.vodovoz.feature.order_call_you

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.getQueryParams
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.order_call_you.model.CallYouItemUi
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouEvent
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouState
import com.m.vodovoz.feature.order_call_you.model.OrderCallYouUiState
import com.m.vodovoz.feature.order_call_you.model.mapToUi
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class OrderCallYouViewModel @Inject constructor(
    val tabManager: TabManager,
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<OrderCallYouState, OrderCallYouEvent>(OrderCallYouState()) {

    private val addressId: Long = savedStateHandle.get<Long>("addressId") ?: -1

    private val callYouId: String? = savedStateHandle.get<String>("callYouId")

    private val queryParams: Map<String, String> = savedStateHandle.getQueryParams()

    init {
        fetchOrderCallYouDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(OrderCallYouEvent.GoBack)
    }

    fun fetchOrderCallYouDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = OrderCallYouUiState.Loading)
        }

        val callYouDetailsResult =
            vodovozServiceRepository.getOrderCallYouDetails(addressId, queryParams).singleResult()

        callYouDetailsResult.onSuccess { callYouDetails ->

            val items = callYouDetails.items.mapToUi()
            val currentItem = items.firstOrNull {
                it.value == callYouId
            }

            updateState { s ->
                s.copy(
                    uiState = OrderCallYouUiState.CallYou,
                    title = callYouDetails.title,
                    currentItem = currentItem ?: CallYouItemUi.Empty,
                    items = items,
                    button = callYouDetails.button.toUi().copy(
                        enabled = currentItem != null
                    )
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(
                    uiState = OrderCallYouUiState.Error
                )
            }

        }
    }

    fun chooseOrderingCallYou(button: ColorfulButtonUi) = viewModelScope.launch {
        sendEvent(OrderCallYouEvent.GoBackToOrdering(stateSnapshot.currentItem))
    }

    fun selectCallYouItem(item: CallYouItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(currentItem = item, button = s.button.copy(enabled = true))
        }
    }
}
