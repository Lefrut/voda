package com.vodovoz.app.data.vodovoz_service.mappers

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.core.network.messageWithCode
import com.vodovoz.app.data.vodovoz_service.model.VodovozResponseDTO
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.domain.general.model.VodovozPlaceholderModel
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

val moshiWithJsonAdapter: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

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
        val body = kotlin.runCatching { adapter.fromJson(stringBody) }.getOrNull()


        if (body != null && response.code() == 200) {
            val result = kotlin.runCatching {
                mapper(body)
            }
            if (result.isFailure) {
                emit(onFail(Response.error(1100, stringBody.jsonToResponseBody())))
            } else {
                emit(result)
            }
        } else {
            emit(onFail(Response.error(response.code(), stringBody.jsonToResponseBody())))
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { throwable -> debugLog { throwable.stackTraceToString() } }
    }.flowOn(Dispatchers.IO)
}

