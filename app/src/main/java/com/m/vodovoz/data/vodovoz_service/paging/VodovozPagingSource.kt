package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.util.extensions.debugLog
import kotlinx.coroutines.flow.singleOrNull
import kotlin.reflect.KType
import kotlin.reflect.javaType

class VodovozPagingSource<T : Any, R : Any>(
    private val executor: VodovozRequestExecutor,
    private val type: KType,
    private val request: suspend (page: Int, limit: Int) -> retrofit2.Response<VodovozResponseDTO<T>>,
    private val mapper: (T) -> PagingSourceData<R>,
) : PagingSource<Int, R>() {

    private companion object {
        const val MIN_ITEMS_FOR_NEXT_PAGE = 3
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> {
        val page = params.key ?: 1

        val result = executor.executeRequestImpl(
            request = { request(page, params.loadSize) },
            mapper = {
                mapper(it.data!!)
            },
            type = type.javaType
        ).singleOrNull()

        if (result == null) {
            return LoadResult.Error(NoSuchElementException("No elements received from the flow"))
        }

        result.onSuccess { pagingData ->
            val items = pagingData.items
            val nextKey = if (items.size > 2 && page < pagingData.pageCount) page + 1 else null

            return LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        }.onFailure { throwable ->
            debugLog { throwable.stackTraceToString() }
            return LoadResult.Error(throwable)
        }

        return LoadResult.Invalid()
    }

    override fun getRefreshKey(state: PagingState<Int, R>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
