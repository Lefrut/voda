package com.m.vodovoz.ui.paging

import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import com.m.vodovoz.ui.mvi.MviViewModel
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

    private val pagingDataViewModelManager = PagingDataViewModelManager(
        scope = viewModelScope,
        onListUpdate = { items ->
            updateState { s -> s.withItems(items) }
        },
        onLoadStateUpdate = { loadStates ->
            updateState { s -> s.withLoadStates(loadStates) }
        }
    )

    fun notifyPaging(itemIndex: Int) = pagingDataViewModelManager.notifyPaging(itemIndex)
    protected suspend fun Flow<PagingData<ITEM>>.collectPagingData() =
        pagingDataViewModelManager.collectPagingData(this)

}

abstract class PagingMviViewModel2<ITEM1 : Any, ITEM2 : Any, S : PagingState2<ITEM1, ITEM2, S>, E>(
    state: S,
) : ItemsMviViewModel2<ITEM1, ITEM2, S, E>(state) {

    private val pagingManager1 = PagingDataViewModelManager<ITEM1>(
        scope = viewModelScope,
        onListUpdate = { items1 ->
            updateState { s -> s.withItems1(items1) }
        },
        onLoadStateUpdate = { loadStates ->
            updateState { s -> s.withLoadStates1(loadStates) }
        }
    )

    private val pagingManager2 = PagingDataViewModelManager<ITEM2>(
        scope = viewModelScope,
        onListUpdate = { items2 ->
            updateState { s -> s.withItems2(items2) }
        },
        onLoadStateUpdate = { loadStates ->
            updateState { s -> s.withLoadStates2(loadStates) }
        }
    )

    fun notifyPaging1(itemIndex: Int) = pagingManager1.notifyPaging(itemIndex)

    fun notifyPaging2(itemIndex: Int) = pagingManager2.notifyPaging(itemIndex)

    suspend fun Flow<PagingData<ITEM1>>.collectPagingData1() =
        pagingManager1.collectPagingData(this)

    suspend fun Flow<PagingData<ITEM2>>.collectPagingData2() =
        pagingManager2.collectPagingData(this)

}

private class PagingDataViewModelManager<T : Any>(
    private val scope: CoroutineScope,
    private val onListUpdate: (List<T>) -> Unit,
    private val onLoadStateUpdate: (CombinedLoadStates) -> Unit,
) {

    private val pagingDataPresenterWrapper = PagingDataPresenterWrapper { snapshotList ->
        onListUpdate(snapshotList.items)
    }

    private fun listenLoadStates() = scope.launch {
        pagingDataPresenterWrapper.collectLoadState { combinedLoadStates ->
            onLoadStateUpdate(combinedLoadStates.copy(refresh = combinedLoadStates.refresh))
        }
    }

    init {
        listenLoadStates()
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
                val result = updateItems(stateSnapshot.items, data)
                updateState { s -> s.withItems(result) }
            }
    }

}

abstract class ItemsMviViewModel2<ITEM : Any, ITEM2 : Any, S : ItemsState2<ITEM, ITEM2, S>, E> protected constructor(
    state: S,
) : MviViewModel<S, E>(state) {


    @Suppress("unused")
    open suspend fun <T> collectItems1With(
        source: Flow<T>,
        updateItems: suspend (List<ITEM>, T) -> List<ITEM>,
    ) {
        state.map { it.items1 }.distinctUntilChanged().combine(source) { _, data ->
            data
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
            .collect { data ->
                val result = updateItems(stateSnapshot.items1, data)
                updateState { s -> s.withItems1(result) }
            }
    }

    @Suppress("unused")
    open suspend fun <T> collectItems2With(
        source: Flow<T>,
        updateItems: suspend (List<ITEM2>, T) -> List<ITEM2>,
    ) {
        state.map { it.items2 }.distinctUntilChanged().combine(source) { _, data ->
            data
        }.shareIn(viewModelScope, SharingStarted.Eagerly, 1)
            .collect { data ->
                val result = updateItems(stateSnapshot.items2, data)
                updateState { s -> s.withItems2(result) }
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

abstract class PagingState2<ITEM1 : Any, ITEM2 : Any, STATE> : ItemsState2<ITEM1, ITEM2, STATE>() {

    abstract val loadStates1: CombinedLoadStates
    abstract val loadStates2: CombinedLoadStates

    fun withLoadStates1(newStates: CombinedLoadStates): STATE =
        copyPagingState(loadStates1 = newStates)

    fun withLoadStates2(newStates: CombinedLoadStates): STATE =
        copyPagingState(loadStates2 = newStates)


    protected abstract fun copyPagingState(
        items1: List<ITEM1> = this.items1,
        items2: List<ITEM2> = this.items2,
        loadStates1: CombinedLoadStates = this.loadStates1,
        loadStates2: CombinedLoadStates = this.loadStates2,
    ): STATE
}


