package com.vodovoz.app.data.vodovoz_service.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.Types
import com.vodovoz.app.core.network.messageWithCode
import com.vodovoz.app.data.vodovoz_service.mappers.executeRequest
import com.vodovoz.app.data.vodovoz_service.model.VodovozResponseDTO
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.util.extensions.debugLog
import kotlinx.coroutines.flow.singleOrNull
import retrofit2.Response
import kotlin.reflect.KClass

class VodovozPagingSource<T : Any, R : Any> constructor(
    private val clazz: KClass<T>,
    private val request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
    private val mapper: (VodovozResponseDTO<T>) -> List<R>,
    private val onFail: (Response<VodovozResponseDTO<T>>) -> Result<List<R>> = { response ->
        val exception = RequestException(response.messageWithCode())
        Result.failure(exception)
    }
) : PagingSource<Int, R>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> {
        val page = params.key ?: 1

        val result = executeRequest(
            request = {
                request(page, params.loadSize)
            },
            mapper = mapper,
            onFail = onFail,
            type = Types.newParameterizedType(VodovozResponseDTO::class.java, clazz.java)
        ).singleOrNull()
            ?: return LoadResult.Error(NoSuchElementException("No elements received from the flow"))

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