package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.util.extensions.debugLog
import kotlinx.coroutines.flow.singleOrNull
import kotlin.reflect.KType
import kotlin.reflect.javaType

class VodovozPagingSource<T : Any, Item : Any>(
    private val executor: VodovozRequestExecutor,
    private val type: KType,
    private val request: suspend (page: Int, limit: Int) -> retrofit2.Response<VodovozResponseDTO<T>>,
    private val mapper: (T) -> PagingSourceData<Item>,
    private val onPageLoaded: (
        page: Int,
        response: VodovozResponseDTO<T>,
        pagingData: PagingSourceData<Item>,
        pageCount: Int,
        endOfPaginationReached: Boolean,
    ) -> Unit = { _, _, _, _, _ -> },
) : PagingSource<Int, Item>() {

    private companion object {
        const val MIN_ITEMS_FOR_NEXT_PAGE = 3
    }

    private var maxPageCount: Int? = null

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        val page = params.key ?: 1
        var loadedResponse: VodovozResponseDTO<T>? = null
        var loadedPagingData: PagingSourceData<Item>? = null

        val result = executor.executeRequestImpl(
            request = { request(page, params.loadSize) },
            mapper = { response ->

                maxPageCount = response.navigation?.totalPages?.takeIf { it > 0 } ?: maxPageCount

                mapper(response.data!!).also { pagingData ->
                    loadedResponse = response
                    loadedPagingData = pagingData
                }
            },
            type = type.javaType
        ).singleOrNull()

        if (result == null) {
            return LoadResult.Error(NoSuchElementException("No elements received from the flow"))
        }

        result.onSuccess { pagingData ->
            val items = pagingData.items
            val pageCount = maxPageCount ?: pagingData.pageCount
            val nextKey = if (items.size >= MIN_ITEMS_FOR_NEXT_PAGE && page < pageCount) {
                page + 1
            } else {
                null
            }
            loadedResponse?.let { response ->
                onPageLoaded(
                    page,
                    response,
                    loadedPagingData ?: pagingData,
                    pageCount,
                    nextKey == null
                )
            }

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

    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
