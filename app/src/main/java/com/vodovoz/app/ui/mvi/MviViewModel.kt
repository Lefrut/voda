package com.vodovoz.app.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.paging.ItemsMviViewModel
import com.vodovoz.app.ui.paging.ItemsState
import com.vodovoz.app.ui.paging.VodovozItemsListeners
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<STATE, EVENT>(state: STATE) : ViewModel() {

    protected val _state = MutableStateFlow(state)

    @Stable
    val state = _state.asStateFlow()

    protected fun updateState(function: (state: STATE) -> STATE) {
        _state.update(function)
    }

    val stateSnapshot get() = _state.value

    private val _events = MutableSharedFlow<EVENT>(0)

    @Stable
    val events = _events.asSharedFlow()

    protected suspend fun sendEvent(event: EVENT) = _events.emit(event)

}

interface Event

interface State

@Composable
fun <STATE, EVENT> MviViewModel<STATE, EVENT>.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<STATE> {
    return state.collectAsStateWithLifecycle(minActiveState = lifecycleState)
}

@Composable
fun <T, ITEM, S, E> T.collectAsState(): State<S>
        where T : ItemsMviViewModel<ITEM, S, E>,
              T : VodovozItemsListeners<ITEM>,
              S : ItemsState<ITEM, S> {
    LifecycleEffect { listenCanViewAdult() }
    LifecycleEffect { listenProductLoadings() }
    LifecycleEffect { listenFavorites() }
    LifecycleEffect { listenCart() }
    return (this as MviViewModel<S, E>).collectAsState()
}
