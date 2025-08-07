package com.vodovoz.app.ui.mvi

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class MviViewModel<STATE, EVENT>(state: STATE): ViewModel() {

    protected val _state = MutableStateFlow(state)
    val state = _state.asStateFlow()

    protected val stateSnapshot get() = _state.value

    private val _events = MutableSharedFlow<EVENT>(0)
    val events = _events.asSharedFlow()

    protected suspend fun sendEvent(event: EVENT) = _events.emit(event)


}