package com.m.vodovoz.data.vodovoz_service.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.m.vodovoz.core.network.retrofit.messageWithCode
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.reflect.KClass

data object VodovozPagerFactory {

    fun <T : Any, R : Any> create(
        clazz: KClass<T>,
        request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        mapper: (T) -> List<R>,
        fail: (Response<ResponseBody>) -> Result<List<R>> = { response ->
            val exception = RequestException(response.messageWithCode())
            Result.failure(exception)
        },
    ): Pager<Int, R> {
        return Pager(
            config = PagingConfig(5),
            pagingSourceFactory = {
                VodovozPagingSource(clazz, request, mapper, fail)
            }
        )
    }

    fun <T : Any, R : Any> getFlow(
        clazz: KClass<T>,
        request: suspend (page: Int, limit: Int) -> Response<VodovozResponseDTO<T>>,
        mapper: (T) -> List<R>,
        fail: (Response<ResponseBody>) -> Result<List<R>> = { response ->
            val exception = RequestException(response.messageWithCode())
            Result.failure(exception)
        },
    ): Flow<PagingData<R>> {
        return create(clazz, request, mapper, fail).flow
    }



}