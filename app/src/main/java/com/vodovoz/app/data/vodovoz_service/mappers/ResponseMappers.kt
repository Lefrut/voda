package com.vodovoz.app.data.vodovoz_service.mappers

import androidx.annotation.Keep
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.core.network.converters.LocalDateTimeJsonAdapter
import com.vodovoz.app.core.network.retrofit.messageWithCode
import com.vodovoz.app.core.network.retrofit.stringBody
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
        .add(KotlinJsonAdapterFactory())
        .build()


@Keep
inline fun <reified T, R> executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline mapper: (T) -> R,
    crossinline onFail: ((Response<ResponseBody>) -> Result<R>) = { response ->
        val exception = RequestException(response.messageWithCode())
        Result.failure(exception)
    },
    crossinline onResponse: (Response<T>) -> Unit = {},
    type: Type = typeOf<T>().javaType,
): Flow<Result<R>> {
    return flow {
        val response = request()

        onResponse(response)

        val stringBody = response.stringBody()

        val bodyResult = kotlin.runCatching {
            val adapter = moshiWithJsonAdapter.adapter<T>(type).lenient()
            adapter.fromJson(stringBody)
        }
        val body = bodyResult.getOrNull()
        val responseCode = response.code()
        val errorCode = responseCode.takeIf { code -> code !in 200..299 } ?: 520

        bodyResult.onSuccess {
            if (body != null && responseCode == 200) {
                val result = kotlin.runCatching { mapper(body) }

                if (result.isFailure) {
                    emit(onFail(Response.error(errorCode, stringBody.jsonToResponseBody())))
                } else {
                    emit(result)
                }
            } else {
                emit(onFail(Response.error(errorCode, stringBody.jsonToResponseBody())))
            }
        }.onFailure {
            val onFailResult = onFail(Response.error(errorCode, stringBody.jsonToResponseBody()))
            emit(onFailResult)
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { t -> debugLog { t.stackTraceToString() } }
    }.flowOn(Dispatchers.IO)
}

