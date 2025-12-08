package com.m.vodovoz.feature.cart.bottles

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.core.android.getList
import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.cart.bottles.model.BottleUi
import com.m.vodovoz.feature.cart.bottles.model.mapToUi
import com.m.vodovoz.feature.cart.bottles.model.toMap
import com.m.vodovoz.feature.cart.bottles.model.updateCartQuantity
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
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
    savedStateHandle: SavedStateHandle,
) : MviViewModel<AllBottlesFlowViewModel.BottlesState, AllBottlesFlowViewModel.BottlesEvent>(
    BottlesState()
) {

    private val cartBottles = savedStateHandle.getList<BottleUi>("bottles")

    init {
        fetchAllBottlesDetails()
        listenBottlesChanges()
    }

    private fun listenBottlesChanges() = viewModelScope.launch {
        state.map { pagingState -> pagingState.bottles }.collectLatest {
            updateState { s ->
                s.copy(
                    hideButton = !s.bottles.any { bottle ->
                        bottle.cartQuantity > 0
                    } || s.isSingleBottleMode
                )
            }
        }
    }

    private inner class SingleBottleMode : ViewModelMode() {

        override suspend fun incrementBottle(bottle: BottleUi) {
            updateBottleCartQuantity(bottle) { 1 }
            updateBottlesOnline().join()
        }

        override suspend fun decrementBottle(bottle: BottleUi) {}

        override fun startLoading() {
            updateState { s ->
                s.copy(uiState = BottlesUiState.Loading)
            }
        }

        override suspend fun handleFail() {
            sendEvent(BottlesEvent.GoBack)
        }
    }

    private inner class MultipleBottleMode : ViewModelMode() {
        override suspend fun incrementBottle(bottle: BottleUi) {
            updateBottleCartQuantity(bottle) { cartQuantity -> cartQuantity + 1 }
        }

        override suspend fun decrementBottle(bottle: BottleUi) {
            updateBottleCartQuantity(bottle) { cartQuantity -> cartQuantity - 1 }
        }

        override fun startLoading() {
            updateState { s ->
                s.copy(buttonIsLoading = true)
            }
        }

        override suspend fun handleFail() {
            updateState { s ->
                s.copy(buttonIsLoading = false)
            }
        }
    }

    private val viewModelMode
        get() = if (stateSnapshot.isSingleBottleMode) {
            SingleBottleMode()
        } else {
            MultipleBottleMode()
        }


    fun fetchAllBottlesDetails() = viewModelScope.launch(Dispatchers.IO) {
        updateState { s -> s.copy(uiState = BottlesUiState.Loading) }

        val allBottlesResult = vodovozServiceRepository.getAllBottles(
        ).singleResult()

        allBottlesResult.onSuccess { allBottlesDetails ->

            val allBottles = allBottlesDetails.bottles.mapToUi()

            val mergedBottles = allBottles.map { bottle ->
                val cartBottle = cartBottles.find { it.id == bottle.id }
                bottle.copy(cartQuantity = cartBottle?.cartQuantity ?: 0)
            }

            updateState { s ->
                s.copy(
                    description = allBottlesDetails.description,
                    isSingleBottleMode = allBottlesDetails.isSingleBottleMode,
                    uiState = BottlesUiState.Success,
                    bottles = mergedBottles
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
            s.copy(
                isSearchMode = searchMode,
                searchQuery = ""
            )
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

    fun incrementBottle(bottle: BottleUi) = viewModelScope.launch {
        viewModelMode.incrementBottle(bottle)
    }

    fun decrementBottle(bottle: BottleUi) = viewModelScope.launch {
        viewModelMode.decrementBottle(bottle)
    }

    private fun updateBottleCartQuantity(
        bottle: BottleUi,
        block: (cartQuantity: Int) -> Int,
    ) {
        updateState { state ->
            state.copy(
                bottles = state.bottles.updateCartQuantity(bottle, block)
            )
        }
    }

    fun updateBottlesOnline() = viewModelScope.launch {
        viewModelMode.startLoading()

        val bottles = stateSnapshot.bottles.filter { it.cartQuantity > 0 }

        val bottlesMap = bottles.toMap()

        val updateBottlesResult = if (bottlesMap != cartBottles.toMap())
            vodovozServiceRepository.replaceMultipleBottlesToCart(
                bottlesMap.toCartProducts()
            ).singleResult()
        else {
            navigateBack()
            return@launch
        }

        updateBottlesResult.onSuccess {
            cartManager.updateRefreshCart(true)
            cartManager.observeRefreshCart().collectLatest { hasUpdates ->
                if (!hasUpdates) {
                    updateState { s ->
                        s.copy(
                            buttonIsLoading = false,
                            uiState = BottlesUiState.Success
                        )
                    }
                    navigateBack()
                }
            }
        }.onFailure {
            viewModelMode.handleFail()
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

    @Stable
    sealed interface BottlesUiState {
        data object Loading : BottlesUiState
        data object Error : BottlesUiState
        data object Success : BottlesUiState
    }

    sealed interface BottlesEvent : Event {
        data object GoBack : BottlesEvent
    }

    private sealed class ViewModelMode {
        abstract suspend fun incrementBottle(bottle: BottleUi)

        abstract suspend fun decrementBottle(bottle: BottleUi)

        abstract fun startLoading()

        abstract suspend fun handleFail()
    }

}