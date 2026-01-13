package com.m.vodovoz.data.vodovoz_service

import androidx.annotation.Keep
import com.m.vodovoz.core.network.retrofit.stringBody
import com.m.vodovoz.core.network.retrofit.stringErrorBody
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.data.vodovoz_service.mappers.jsonToResponseBody
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.util.extensions.catchResult
import com.m.vodovoz.util.extensions.debugLog
import com.m.vodovoz.util.extensions.decodeUnicodeEscapes
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

abstract class RequestExecutor(
    private val moshi: Moshi
) {

    protected open fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
        throw IllegalStateException("fail not implemented")
    }

    open fun <T : Any, R : Any> executeRequestImpl(
        request: suspend () -> Response<T>,
        response: (Response<T>) -> Unit = {},
        mapper: (T) -> R,
        fail: (Response<ResponseBody>) -> Result<R> = ::onFail,
        type: Type,
    ): Flow<Result<R>> {
        return flow {
            val requestResponse = request()

            response(requestResponse)

            val stringBody = requestResponse.stringBody().ifEmpty {
                requestResponse.stringErrorBody()
            }.decodeUnicodeEscapes()

            val bodyResult = kotlin.runCatching {
                moshi.fromJson<T>(stringBody, type)
            }
            val responseCode = requestResponse.code()

            val errorCode = responseCode.takeIf { code ->
                code !in 200..299
            } ?: 520

            val finalResult = bodyResult.mapCatching { body ->
                require(responseCode == 200) { "Bad response code: $responseCode" }
                mapper(body)
            }.recoverCatching {
                fail(Response.error(errorCode, stringBody.jsonToResponseBody())).getOrThrow()
            }

            emit(finalResult)
        }.catchResult().take(1).onEach { result ->
            debugLog {
                result.onFailure { t ->
                    return@debugLog t.stackTraceToString()
                }
            }

        }

    }

}

open class VodovozRequestExecutor(moshi: Moshi) : RequestExecutor(moshi)

open class BaseRequestExecutor(moshi: Moshi) : RequestExecutor(moshi)


inline fun <reified T : Any, R : Any> BaseRequestExecutor.executeRequest(
    noinline request: suspend () -> Response<T>,
    noinline response: (Response<T>) -> Unit = {},
    noinline mapper: (T) -> R,
): Flow<Result<R>> {
    return executeRequestImpl(
        request = request,
        response = response,
        mapper = mapper,
        type = typeOf<T>().javaType
    )
}

@Keep
inline fun <reified T : Any?, reified R : Any> VodovozRequestExecutor.executeRequest(
    noinline request: suspend () -> Response<VodovozResponseDTO<T>>,
    noinline response: (Response<VodovozResponseDTO<T>>) -> Unit = {},
    crossinline toDomain: T.() -> R = { this as R },
    noinline mapper: (VodovozResponseDTO<T>) -> R = { vodovozResponse ->
        toDomain((vodovozResponse.data ?: "") as T)
    },
    type: Type = typeOf<VodovozResponseDTO<T>>().javaType,
): Flow<Result<R>> {
    return executeRequestImpl(
        request = request,
        response = response,
        mapper = mapper,
        type = type
    )
}


