package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.util.extensions.debugLog
import kotlinx.coroutines.flow.singleOrNull
import retrofit2.Response
import kotlin.reflect.KType
import kotlin.reflect.javaType

class VodovozPagingSource<T : Any, R : Any>(
    private val executor: VodovozRequestExecutor,
    private val type: KType,
    private val request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
    private val mapper: (T) -> List<R>,
) : PagingSource<Int, R>() {

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

        result.onSuccess { list ->
            val nextKey = if (list.size < 3) null else page + 1

            return LoadResult.Page(
                data = list,
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