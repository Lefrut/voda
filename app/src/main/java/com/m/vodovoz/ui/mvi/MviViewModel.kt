package com.m.vodovoz.ui.mvi

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.paging.ItemsMviViewModel
import com.m.vodovoz.ui.paging.ItemsMviViewModel2
import com.m.vodovoz.ui.paging.ItemsState
import com.m.vodovoz.ui.paging.ItemsState2
import com.m.vodovoz.ui.paging.VodovozItemsListeners
import com.m.vodovoz.ui.paging.VodovozItemsListeners2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun ViewModel.launchInViewModelScope(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
) = viewModelScope.launch(
    context = context,
    start = start,
    block = block
)

abstract class MviViewModel<STATE, EVENT>(state: STATE) : ViewModel() {

    private val _state = MutableStateFlow(state)

    @Stable
    val state = _state.asStateFlow()

    protected fun updateState(function: (state: STATE) -> STATE) {
        _state.update(function)
    }

    val stateSnapshot get() = state.value

    private val _events = MutableSharedFlow<EVENT>(0)

    @Stable
    val events = _events.asSharedFlow()

    protected suspend fun sendEvent(event: EVENT) = _events.emit(event)

    @VisibleForTesting
    internal fun setState(state: STATE) {
        _state.update { state }
    }

}

interface Event

interface State

@Composable
@Stable
fun <STATE, EVENT> MviViewModel<STATE, EVENT>.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<STATE> {
    return state.collectAsStateWithLifecycle(minActiveState = lifecycleState)
}


@Composable
@Stable
fun <T, ITEM, S, E> T.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<S>
        where T : ItemsMviViewModel<ITEM, S, E>,
              T : VodovozItemsListeners<ITEM>,
              S : ItemsState<ITEM, S> {
    LifecycleEffect { listenCanViewAdult() }
    LifecycleEffect { listenProductLoadings() }
    LifecycleEffect { listenFavorites() }
    LifecycleEffect { listenCart() }
    return (this as MviViewModel<S, E>).collectAsState(lifecycleState)
}

@Composable
@Stable
fun <T, ITEM1, ITEM2, S, E> T.collectAsState(
    lifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
): State<S>
        where T : ItemsMviViewModel2<ITEM1, ITEM2, S, E>,
              T : VodovozItemsListeners2<ITEM1, ITEM2>,
              S : ItemsState2<ITEM1, ITEM2, S> {
    LifecycleEffect { listenCanViewAdult1() }
    LifecycleEffect { listenProductLoadings1() }
    LifecycleEffect { listenFavorites1() }
    LifecycleEffect { listenCart1() }
    LifecycleEffect { listenCanViewAdult2() }
    LifecycleEffect { listenProductLoadings2() }
    LifecycleEffect { listenFavorites2() }
    LifecycleEffect { listenCart2() }
    return (this as MviViewModel<S, E>).collectAsState(lifecycleState)
}


@SuppressLint("ComposableNaming")
@Composable
fun <S, E> MviViewModel<S, E>.collectEvents(
    handler: suspend (E) -> Unit
) {
    LifecycleEffect {
        events.collect { handler(it) }
    }
}


