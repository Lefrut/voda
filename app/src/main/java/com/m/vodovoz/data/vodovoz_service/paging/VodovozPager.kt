package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.domain.general.model.paging.VodovozPagesInfo
import com.m.vodovoz.domain.general.model.paging.VodovozPagingMeta
import com.m.vodovoz.domain.general.model.paging.VodovozPagingResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Response
import kotlin.reflect.typeOf

data object VodovozPagerFactory {

    inline fun <reified T : Any, Item : Any> create(
        executor: VodovozRequestExecutor,
        noinline request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        noinline mapper: (T) -> PagingSourceData<Item>,
        noinline onPageLoaded: (
            page: Int,
            response: VodovozResponseDTO<T>,
            pagingData: PagingSourceData<Item>,
            pageCount: Int,
            endOfPaginationReached: Boolean,
        ) -> Unit = { _, _, _, _, _ -> },
        noinline onPageError: (page: Int, error: Throwable) -> Unit = { _, _ -> },
    ): Pager<Int, Item> {
        return Pager(
            config = PagingConfig(5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    executor = executor,
                    type = typeOf<VodovozResponseDTO<T>>(),
                    request = request,
                    mapper = mapper,
                    onPageLoaded = onPageLoaded,
                    onPageError = onPageError,
                )
            }
        )
    }

    inline fun <reified T : Any, Item : Any> getFlow(
        executor: VodovozRequestExecutor,
        noinline request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        noinline mapper: (T) -> PagingSourceData<Item>,
    ): Flow<PagingData<Item>> {
        return create(executor, request, mapper).flow
    }

    inline fun <reified T : Any, Item : Any, Meta> getResult(
        executor: VodovozRequestExecutor,
        noinline request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        noinline mapper: (T) -> PagingSourceData<Item>,
        noinline metaMapper: (T) -> Meta?,
    ): VodovozPagingResult<Item, Meta> {
        val pagingMeta = MutableStateFlow(VodovozPagingMeta<Meta>())
        val pager = create(
            executor = executor,
            request = request,
            mapper = mapper,
            onPageLoaded = { page, response, _, pageCount, endOfPaginationReached ->
                val firstPage = if (page == 1) response.data?.let(metaMapper) else null
                pagingMeta.update { current ->
                    current.copy(
                        firstPageData = if (page == 1) firstPage else current.firstPageData,
                        firstPageError = if (page == 1) null else current.firstPageError,
                        pagesInfo = VodovozPagesInfo(
                            currentPage = page,
                            pageCount = pageCount,
                            endOfPaginationReached = endOfPaginationReached
                        )
                    )
                }
            },
            onPageError = { page, error ->
                if (page == 1) {
                    pagingMeta.update { it.copy(firstPageData = null, firstPageError = error) }
                }
            }
        )

        return VodovozPagingResult(
            flow = pager.flow,
            meta = pagingMeta
        )
    }
}
