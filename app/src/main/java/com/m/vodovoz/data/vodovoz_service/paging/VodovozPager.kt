package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import kotlin.reflect.typeOf

data object VodovozPagerFactory {

    inline fun <reified T : Any, R : Any> create(
        executor: VodovozRequestExecutor,
        noinline request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        noinline mapper: (T) -> PagingSourceData<R>,
        noinline pageCountProvider: (VodovozResponseDTO<T>) -> Int? = { null },
    ): Pager<Int, R> {
        return Pager(
            config = PagingConfig(5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    executor = executor,
                    type = typeOf<VodovozResponseDTO<T>>(),
                    request = request,
                    pageCountProvider = pageCountProvider,
                    mapper = mapper,
                )
            }
        )
    }

    inline fun <reified T : Any, R : Any> getFlow(
        executor: VodovozRequestExecutor,
        noinline request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        noinline mapper: (T) -> PagingSourceData<R>,
        noinline pageCountProvider: (VodovozResponseDTO<T>) -> Int? = { null },
    ): Flow<PagingData<R>> {
        return create(executor, request, mapper, pageCountProvider).flow
    }
}
