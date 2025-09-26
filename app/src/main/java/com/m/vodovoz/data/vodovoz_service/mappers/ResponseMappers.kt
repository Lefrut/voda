package com.m.vodovoz.data.vodovoz_service.mappers

import androidx.annotation.Keep
import com.m.vodovoz.common.moshi.adapter.LocalDateTimeJsonAdapter
import com.m.vodovoz.core.network.retrofit.messageWithCode
import com.m.vodovoz.core.network.retrofit.stringBody
import com.m.vodovoz.core.network.retrofit.stringErrorBody
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import com.m.vodovoz.util.extensions.catchResult
import com.m.vodovoz.util.extensions.debugLog
import com.m.vodovoz.util.extensions.decodeUnicodeEscapes
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okio.buffer
import okio.source
import retrofit2.Response
import java.lang.reflect.Type
import java.time.LocalDateTime
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

fun String.jsonToResponseBody(): ResponseBody {
    val mediaType = "application/json".toMediaType()
    val bytes = toByteArray()
    return object : ResponseBody() {
        override fun contentType() = mediaType
        override fun contentLength() = bytes.size.toLong()
        override fun source() = bytes.inputStream().source().buffer()
    }
}

val moshiWithJsonAdapter: Moshi =
    Moshi.Builder()
        .add(
            LocalDateTime::class.java,
            LocalDateTimeJsonAdapter().nullSafe()
        )
        .add(KotlinJsonAdapterFactory())
        .build()


@Keep
inline fun <reified T : Any, R> executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline response: (Response<T>) -> Unit = {},
    crossinline mapper: (T) -> R,
    crossinline fail: ((Response<ResponseBody>) -> Result<R>) = { r ->
        val exception = RequestException(r.messageWithCode())
        Result.failure(exception)
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
            moshiWithJsonAdapter.fromJson<T>(stringBody, type)
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



