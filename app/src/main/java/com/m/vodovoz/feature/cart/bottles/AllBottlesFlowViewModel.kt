package com.m.vodovoz.feature.cart.bottles

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.cart.bottles.model.BottleUi
import com.m.vodovoz.feature.cart.bottles.model.mapToUi
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
@HiltViewModel
class AllBottlesFlowViewModel @Inject constructor(
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<AllBottlesFlowViewModel.BottlesState, AllBottlesFlowViewModel.BottlesEvent>(
    BottlesState()
) {


    init {
        fetchAllBottlesDetails()
        listenBottlesChanges()
    }

    private fun listenBottlesChanges() = viewModelScope.launch {
        state.map { pagingState -> pagingState.bottles }.collectLatest {
            updateState { s ->
                s.copy(hideButton = !s.bottles.any { bottle -> bottle.cartQuantity > 0 } || s.isSingleBottleMode)
            }
        }
    }

    fun fetchAllBottlesDetails() = viewModelScope.launch(Dispatchers.IO) {
        updateState { s ->
            s.copy(uiState = BottlesUiState.Loading)
        }

        val allBottlesResult = vodovozServiceRepository.getAllBottles().singleResult()

        allBottlesResult.onSuccess { allBottlesDetails ->
            updateState { s ->
                s.copy(
                    description = allBottlesDetails.description,
                    isSingleBottleMode = allBottlesDetails.isSingleBottleMode,
                    uiState = BottlesUiState.Success,
                    bottles = allBottlesDetails.bottles.mapToUi()
                )
            }
        }.onFailure {
            updateState { s ->
                s.copy(uiState = BottlesUiState.Error)
            }
        }
    }

    fun changeSearchMode(searchMode: Boolean) = viewModelScope.launch {
        updateState { s ->
            s.copy(isSearchMode = searchMode, searchQuery = "")
        }
    }

    fun changeSearchQuery(newQuery: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(searchQuery = newQuery)
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(BottlesEvent.GoBack)
    }

    fun addBottle(bottle: BottleUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                bottles = s.bottles.map { b -> if (b.id == bottle.id) b.copy(cartQuantity = 1) else b }
            )
        }

        if (stateSnapshot.isSingleBottleMode) {
            addBottlesToCart()
            return@launch
        }
    }

    fun incrementBottle(bottle: BottleUi) = viewModelScope.launch {
        if (stateSnapshot.isSingleBottleMode) return@launch

        updateState { s ->
            s.copy(bottles = s.bottles.map { if (it.id == bottle.id) it.copy(cartQuantity = it.cartQuantity + 1) else it })
        }

    }

    fun decrementBottle(bottle: BottleUi) = viewModelScope.launch {
        if (bottle.cartQuantity <= 0 || stateSnapshot.isSingleBottleMode) return@launch

        updateState { s ->
            s.copy(
                bottles = s.bottles.map {
                    if (it.id == bottle.id) it.copy(cartQuantity = it.cartQuantity - 1)
                    else it
                }
            )
        }
    }

    fun addBottlesToCart() = viewModelScope.launch {
        if (stateSnapshot.isSingleBottleMode) {
            updateState { s ->
                s.copy(uiState = BottlesUiState.Loading)
            }
        } else {
            updateState { s ->
                s.copy(
                    buttonIsLoading = true
                )
            }
        }


        val bottlesMap = stateSnapshot.bottles.associate {
            it.id to it.cartQuantity
        }.filter { it.value > 0 }

        val addBottlesResult = vodovozServiceRepository
            .addMultipleProductsToCart(cartManager.formatCart(bottlesMap))
            .singleResult()

        addBottlesResult.onSuccess {
            cartManager.updateRefreshCart(true)
            cartManager.observeRefreshCart().collectLatest { hasUpdates ->
                if (!hasUpdates) {
                    updateState { s -> s.copy(buttonIsLoading = false) }
                    sendEvent(BottlesEvent.GoBack)
                }
            }
        }.onFailure {
            updateState { s ->
                s.copy(buttonIsLoading = false)
            }
            if (stateSnapshot.isSingleBottleMode) {
                sendEvent(BottlesEvent.GoBack)
            }
        }


    }


    @Immutable
    data class BottlesState(
        val description: String = "",
        val bottles: List<BottleUi> = emptyList(),
        val isSingleBottleMode: Boolean = false,
        val uiState: BottlesUiState = BottlesUiState.Loading,
        val searchQuery: String = "",
        val isSearchMode: Boolean = false,
        val hideButton: Boolean = true,
        val buttonIsLoading: Boolean = false,
    ) : State

    @Immutable
    sealed interface BottlesUiState {
        data object Loading : BottlesUiState
        data object Error : BottlesUiState
        data object Success : BottlesUiState
    }

    sealed interface BottlesEvent : Event {
        data object GoBack : BottlesEvent
    }
}