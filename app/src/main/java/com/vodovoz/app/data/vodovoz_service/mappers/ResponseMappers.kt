package com.vodovoz.app.data.vodovoz_service.mappers

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.core.network.converters.LocalDateTimeJsonAdapter
import com.vodovoz.app.core.network.messageWithCode
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.util.extensions.catchResult
import com.vodovoz.app.util.extensions.debugLog
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
        .add(LocalDateTime::class.java, LocalDateTimeJsonAdapter().nullSafe())
        .add(KotlinJsonAdapterFactory()).build()

inline fun <reified T, R> executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline mapper: (T) -> R,
    noinline onFail: ((Response<T>) -> Result<R>) = { response ->
        val exception = RequestException(response.messageWithCode() ?: "")
        Result.failure(exception)
    },
    crossinline onResponse: (Response<T>) -> Unit = {},
    type: Type = typeOf<T>().javaType,
): Flow<Result<R>> {
    return flow {
        val response = request()
        onResponse(response)

        val adapter = moshiWithJsonAdapter.adapter<T>(type)

        val stringBody = (response.body() as? String) ?: ""
        val bodyResult = kotlin.runCatching { adapter.fromJson(stringBody) }
        val body = bodyResult.getOrNull()
        val responseCode = response.code()

        bodyResult.onSuccess {
            if (body != null && responseCode == 200) {
                val result = kotlin.runCatching {
                    mapper(body)
                }
                if (result.isFailure) {
                    emit(onFail(Response.error(1100, stringBody.jsonToResponseBody())))
                } else {
                    debugLog { result.onFailure { t -> t.toString() + t.stackTraceToString() } }

                    emit(result)
                }
            } else {
                emit(onFail(Response.error(responseCode.takeIf { it != 200 } ?: 1100,
                    stringBody.jsonToResponseBody())))
            }
        }.onFailure {
            debugLog {
                bodyResult.onFailure { t ->
                    t.toString() + t.stackTraceToString()
                }
            }

            emit(onFail(Response.error(responseCode.takeIf { it != 200 } ?: 1100, stringBody.jsonToResponseBody())))
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { t ->
            debugLog { t.stackTraceToString() }
        }
    }.flowOn(Dispatchers.IO)
}

