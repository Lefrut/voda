package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.m.vodovoz.core.network.retrofit.messageWithCode
import com.m.vodovoz.data.vodovoz_service.RequestExecutor
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.reflect.KClass

data object VodovozPagerFactory {

    fun <T : Any, R : Any> create(
        executor: VodovozRequestExecutor,
        clazz: KClass<T>,
        request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        mapper: (T) -> List<R>
    ): Pager<Int, R> {
        return Pager(
            config = PagingConfig(5),
            pagingSourceFactory = {
                VodovozPagingSource(executor, clazz, request, mapper)
            }
        )
    }

    fun <T : Any, R : Any> getFlow(
        executor: VodovozRequestExecutor,
        clazz: KClass<T>,
        request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        mapper: (T) -> List<R>,
    ): Flow<PagingData<R>> {
        return create(executor, clazz, request, mapper).flow
    }


}