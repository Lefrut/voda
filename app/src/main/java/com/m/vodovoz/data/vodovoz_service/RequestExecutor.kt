package com.m.vodovoz.data.vodovoz_service

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

interface RequestExecutor { val moshi: Moshi }

abstract class VodovozRequestExecutor(override val moshi: Moshi) : RequestExecutor

inline fun <reified T : Any, R> RequestExecutor.executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline response: (Response<T>) -> Unit = {},
    crossinline mapper: (T) -> R,
    crossinline fail: ((Response<ResponseBody>) -> Result<R>) = { _ ->
        throw IllegalStateException("Not implemented")
    },
    type: Type = typeOf<T>().javaType,
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
            mapper(body) ?: throw IllegalStateException("Mapper returned null")
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


inline fun <reified T : Any, reified T2 : VodovozResponseDTO<T>, R> VodovozRequestExecutor.executeRequest(
    crossinline request: suspend () -> Response<T2>,
    crossinline response: (Response<T2>) -> Unit = {},
    crossinline mapper: (T) -> R,
    crossinline fail: ((Response<ResponseBody>) -> Result<R>) = { _ ->
        throw IllegalStateException("Not implemented")
    },
    type: Type = typeOf<T2>().javaType,
): Flow<Result<R>> = (this as RequestExecutor).executeRequest(
    request = request,
    response = response,
    mapper = { t2 ->
        mapper(
            t2.data ?: throw IllegalStateException(
                "VodovozResponseDTO.data field is null"
            )
        )
    },
    fail = fail,
    type = type
)
