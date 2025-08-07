package com.vodovoz.app.common.content

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


abstract class PagingContractViewModel<S : State, E : Event>(
    idleState: S,
) : ViewModel() {

    protected val uiStateListener = MutableStateFlow(PagingState.idle(idleState))
    protected val state
        get() = uiStateListener.value

    val stateSnapshot get() = state.data

    @Stable
    fun observeUiState() = uiStateListener.asStateFlow()

    protected open val eventListener = MutableSharedFlow<E>()

    @Stable
    fun observeEvent() = eventListener.asSharedFlow()
}

fun <S> MutableStateFlow<PagingState<S>>.updateData(block: (S) -> S) {
    update { pagingState ->
        pagingState.copy(
            data = block(pagingState.data)
        )
    }
}


interface Event

@Stable
data class PagingState<S>(
    val data: S
) {
    companion object {
        fun <S> idle(idleState: S): PagingState<S> {
            return PagingState(data = idleState)
        }
    }
}

interface State