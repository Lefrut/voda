package com.vodovoz.app.ui.paging

import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.debugLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

abstract class PagingMviViewModel<ITEM : Any, S : PagingState<ITEM, S>, E>(
    state: S,
) : ItemsMviViewModel<ITEM, S, E>(state) {

    private val pagingDataListener = PagingDataListener { snapshotList ->
        updateState { it.withItems(snapshotList.mapNotNull { item -> item }) }
    }

    init {
        listenLoadStates()
    }

    private fun listenLoadStates() = viewModelScope.launch {
        pagingDataListener.collectLoadState { combinedLoadStates ->
            updateState {
                it.withLoadStates(
                    combinedLoadStates.copy(refresh = combinedLoadStates.refresh)
                )
            }
        }
    }

    fun notifyPaging(itemIndex: Int) = viewModelScope.launch {
        kotlin.runCatching { pagingDataListener[itemIndex] }
    }

    suspend fun collectPagingData(pagingData: PagingData<ITEM>) {
        pagingDataListener.collectPagingData(pagingData)
    }

    suspend fun Flow<PagingData<ITEM>>.collectPagingData() = collect { pagingData ->
        pagingDataListener.collectPagingData(pagingData)
    }
}


abstract class ItemsMviViewModel<ITEM : Any, S : ItemsState<ITEM, S>, E> protected constructor(state: S) :
    MviViewModel<S, E>(state) {


    open suspend fun <T> collectItemsWith(
        source: Flow<T>,
        updateItems: suspend (List<ITEM>, T) -> List<ITEM>,
    ) {
        state.map { it.items }.distinctUntilChanged().combine(source) { _, data ->
            data
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
            .collect { data ->
                debugLog { "collectItemsWith, data - $data" }
                val result = updateItems(stateSnapshot.items, data)
                updateState { it.withItems(result) }
            }
    }

}


abstract class ItemsState<ITEM : Any, STATE> {

    abstract val items: List<ITEM>

    abstract fun withItems(newItems: List<ITEM>): STATE

}

abstract class PagingState<ITEM : Any, STATE> : ItemsState<ITEM, STATE>() {

    abstract val loadStates: CombinedLoadStates

    override fun withItems(newItems: List<ITEM>): STATE = copyPagingState(items = newItems)

    fun withLoadStates(newStates: CombinedLoadStates): STATE =
        copyPagingState(loadStates = newStates)

    protected abstract fun copyPagingState(
        items: List<ITEM> = this.items,
        loadStates: CombinedLoadStates = this.loadStates,
    ): STATE
}

