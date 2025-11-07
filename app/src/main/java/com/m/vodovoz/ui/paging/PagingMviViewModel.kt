package com.m.vodovoz.ui.paging

import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.debugLog
import kotlinx.coroutines.CoroutineScope
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

    private val pagingDataPresenterWrapper = PagingDataPresenterWrapper { snapshotList ->
        updateState { it.withItems(snapshotList.mapNotNull { item -> item }) }
    }

    init {
        listenLoadStates()
    }

    private fun listenLoadStates() = viewModelScope.launch {
        pagingDataPresenterWrapper.collectLoadState { combinedLoadStates ->
            updateState { s ->
                s.withLoadStates(
                    combinedLoadStates.copy(refresh = combinedLoadStates.refresh)
                )
            }
        }
    }

    fun notifyPaging(itemIndex: Int) = viewModelScope.launch {
        kotlin.runCatching { pagingDataPresenterWrapper[itemIndex] }
    }

    protected suspend fun Flow<PagingData<ITEM>>.collectPagingData() = collect { pagingData ->
        pagingDataPresenterWrapper.collectPagingData(pagingData)
    }
}

abstract class PagingMviViewModel2<ITEM : Any, ITEM2 : Any, S : PagingState<ITEM, S>, E>(
    state: S,
) : ItemsMviViewModel<ITEM, S, E>(state) {

    private val pagingManager1 = PagingDataViewModelManager<ITEM>(
        scope = viewModelScope,
        onNewItems = {

        },
        onNewLoadStates = {

        }
    )

    private val pagingManager2 = PagingDataViewModelManager<ITEM2>(
        scope = viewModelScope,
        onNewItems = {

        },
        onNewLoadStates = {

        }
    )

    fun notifyPaging1(itemIndex: Int) = pagingManager1.notifyPaging(itemIndex)

    fun notifyPaging2(itemIndex: Int) = pagingManager2.notifyPaging(itemIndex)

    suspend fun Flow<PagingData<ITEM>>.collectPagingData1() = pagingManager1.collectPagingData(this)
    suspend fun Flow<PagingData<ITEM2>>.collectPagingData2() = pagingManager2.collectPagingData(this)

}

private class PagingDataViewModelManager<T : Any>(
    private val scope: CoroutineScope,
    private val onNewItems: (List<T>) -> Unit,
    private val onNewLoadStates: (CombinedLoadStates) -> Unit,
) {

    init {
        listenLoadStates()
    }

    private val pagingDataPresenterWrapper = PagingDataPresenterWrapper<T> { snapshotList ->
        onNewItems(snapshotList.items)
    }

    private fun listenLoadStates() = scope.launch {
        pagingDataPresenterWrapper.collectLoadState { combinedLoadStates ->
            onNewLoadStates(combinedLoadStates.copy(refresh = combinedLoadStates.refresh))
        }
    }

    fun notifyPaging(itemIndex: Int) = scope.launch {
        kotlin.runCatching { pagingDataPresenterWrapper[itemIndex] }
    }

    suspend fun collectPagingData(pagingDataFlow: Flow<PagingData<T>>) =
        pagingDataFlow.collect { pagingData ->
            pagingDataPresenterWrapper.collectPagingData(pagingData)
        }

}


abstract class ItemsMviViewModel<ITEM : Any, S : ItemsState<ITEM, S>, E> protected constructor(state: S) :
    MviViewModel<S, E>(state) {


    @Suppress("unused")
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

abstract class ItemsState2<ITEM1 : Any, ITEM2 : Any, STATE> {

    abstract val items1: List<ITEM1>
    abstract val items2: List<ITEM2>

    abstract fun withItems1(newItems: List<ITEM1>): STATE
    abstract fun withItems2(newItems: List<ITEM2>): STATE

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

abstract class PagingState2<ITEM : Any, STATE> : ItemsState<ITEM, STATE>() {

    abstract val loadStates: CombinedLoadStates

    override fun withItems(newItems: List<ITEM>): STATE = copyPagingState(items = newItems)

    fun withLoadStates(newStates: CombinedLoadStates): STATE =
        copyPagingState(loadStates = newStates)

    protected abstract fun copyPagingState(
        items: List<ITEM> = this.items,
        loadStates: CombinedLoadStates = this.loadStates,
    ): STATE
}


