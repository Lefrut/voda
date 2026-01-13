package com.m.vodovoz.ui.paging

import androidx.compose.runtime.Stable
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull


class PagingDataPresenterWrapper<T : Any>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val onUpdateItems: suspend (ItemSnapshotList<T>) -> Unit,
) {

    private val pagingDataPresenter = object : PagingDataPresenter<T>(dispatcher) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
            onUpdateItems(snapshot())
        }
    }

    operator fun get(index: Int): T? = kotlin.runCatching { pagingDataPresenter[index] }.getOrNull()

    suspend fun collectPagingData(pagingData: PagingData<T>) {
        pagingDataPresenter.collectFrom(pagingData)
    }

    suspend fun collectLoadState(block: suspend (CombinedLoadStates) -> Unit) {
        pagingDataPresenter.loadStateFlow.filterNotNull().collect { combinedLoadStates ->
            block(combinedLoadStates)
        }
    }


}

@Stable
val emptyCombinedLoadStates = CombinedLoadStates(
    refresh = LoadState.NotLoading(endOfPaginationReached = false),
    prepend = LoadState.NotLoading(endOfPaginationReached = false),
    append = LoadState.NotLoading(endOfPaginationReached = false),
    source = LoadStates(
        refresh = LoadState.NotLoading(endOfPaginationReached = false),
        prepend = LoadState.NotLoading(endOfPaginationReached = false),
        append = LoadState.NotLoading(endOfPaginationReached = false)
    ),
    mediator = null
)

fun CombinedLoadStates.copy(
    refresh: LoadState = this.refresh,
    prepend: LoadState = this.prepend,
    append: LoadState = this.append,
    source: LoadStates = this.source,
    mediator: LoadStates? = this.mediator,
): CombinedLoadStates {
    return CombinedLoadStates(refresh, prepend, append, source, mediator)
}

val LoadState.errorOrNull
    get() = this as? LoadState.Error
