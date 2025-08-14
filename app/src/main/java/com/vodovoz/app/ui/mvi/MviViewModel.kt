package com.vodovoz.app.ui.mvi

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
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

    protected val stateSnapshot get() = _state.value

    private val _events = MutableSharedFlow<EVENT>(0)

    @Stable
    val events = _events.asSharedFlow()

    protected suspend fun sendEvent(event: EVENT) = _events.emit(event)

}

interface Event

interface State