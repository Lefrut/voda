package com.vodovoz.app.data.vodovoz_service.mappers

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.core.network.messageWithCode
import com.vodovoz.app.core.network.stringBody
import com.vodovoz.app.data.vodovoz_service.model.SiteStateResponseDTO
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
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer
import okio.source
import retrofit2.Response
import java.lang.reflect.Type
import kotlin.reflect.javaType
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


fun <T, R> executeRequest(
    type: Type,
    request: suspend () -> Response<T>,
    mapper: (T) -> List<R>,
    onFail: ((Response<T>) -> Result<List<R>>) = { response ->
        val exception = RequestException(response.messageWithCode() ?: "")
        Result.failure(exception)
    },
): Flow<Result<List<R>>> {
    return flow {
        val response = request()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<T>(type)

        val stringBody = response.stringBody()
        val body = kotlin.runCatching { adapter.fromJson(stringBody) }.getOrNull()

        if (response.isSuccessful && body != null) {
            val result = kotlin.runCatching {
                mapper(body)
            }
            if (result.isFailure) {
                emit(onFail(Response.error(1100, stringBody.jsonToResponseBody())))
            } else {
                emit(result)
            }
        } else {
            emit(onFail(Response.error(1100, stringBody.jsonToResponseBody())))
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { throwable -> debugLog { throwable.stackTraceToString() } }
    }.flowOn(Dispatchers.IO)
}


@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T, R> executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline mapper: (T) -> R,
    noinline onFail: ((Response<T>) -> Result<R>) = { response ->
        val exception = RequestException(response.messageWithCode() ?: "")
        Result.failure(exception)
    },
): Flow<Result<R>> {
    return flow {
        val response = request()

        val adapter = moshiWithJsonAdapter.adapter<T>(typeOf<T>().javaType)

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

inline fun <T, R> executeRequest(
    crossinline request: suspend () -> Response<T>,
    crossinline mapper: (T) -> R,
    crossinline onResponse: (Response<T>) -> Unit = {},
    noinline onFail: ((Response<T>) -> Result<R>)? = null,
): Flow<Result<R>> {
    return flow {
        val response = request()
        val body = response.body()

        onResponse(response)
        if (response.isSuccessful && body != null) {
            val result = kotlin.runCatching {
                mapper(body)
            }

            if (result.isFailure && onFail != null) {
                emit(onFail(response))
            } else {
                emit(result)
            }
        } else if (onFail != null) {
            emit(onFail(response))
        } else {
            val exception = RequestException(response.messageWithCode())
            emit(Result.failure(exception))
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { throwable -> debugLog { throwable.stackTraceToString() } }
    }.flowOn(Dispatchers.IO)
}


inline fun <T, R> executeVodovozRequest(
    crossinline request: suspend () -> Response<VodovozResponseDTO<T>>,
    crossinline mapper: (VodovozResponseDTO<T>?) -> R,
    noinline onFail: ((Response<VodovozResponseDTO<T>>) -> Result<R>)? = null,
): Flow<Result<R>> {
    return flow {
        val response = request()
        val body = response.body()

        if (response.isSuccessful) {
            val result = kotlin.runCatching {
                mapper(body)
            }
            if (result.isFailure && onFail != null) {
                emit(onFail(response))
            } else {
                emit(result)
            }
        } else if (onFail != null) {
            emit(onFail(response))
        } else {
            val exception = RequestException(response.messageWithCode())
            emit(Result.failure(exception))
        }
    }.catchResult().take(1).onEach { result ->
        result.onFailure { throwable -> debugLog { throwable.stackTraceToString() } }
    }.flowOn(Dispatchers.IO)
}


inline fun <T> VodovozResponseDTO<T>.checkError(
    throwError: (VodovozPlaceholderModel) -> Nothing = { it ->
        throw EmptyResultException(errorData = it, message = message ?: "")
    },
) {
    val errorModel = this.error?.toDomain() ?: return
    throwError(errorModel)
}


