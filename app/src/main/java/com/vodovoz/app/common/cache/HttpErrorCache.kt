package com.vodovoz.app.common.cache

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.vodovoz.app.core.network.serialization.fromJson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton


val emptyHttpErrorCacheMappers = object : HttpErrorCacheMappers<HttpError> {
    override fun String.toHttpError(): HttpError = object : HttpError() {}
}

val emptyHttpErrorCache = object : HttpErrorCache(emptyHttpErrorCacheMappers) {}

@Singleton
class VodovozHttpErrorCache @Inject constructor(
    mappers: HttpErrorCacheMappers<VodovozHttpError>,
) : HttpErrorCache(mappers)

abstract class HttpErrorCache(
    mappers: HttpErrorCacheMappers<*>,
) {
    private val _lastErrorData: MutableStateFlow<String?> = MutableStateFlow(null)

    val lastHttpError: Flow<HttpError?> = _lastErrorData.map { s ->
        kotlin.runCatching {
            with(mappers) { s?.toHttpError() }
        }.getOrNull()
    }

    fun setLastError(errorData: String?) {
        _lastErrorData.update { errorData }

    }
}

@Singleton
class VodovozHttpErrorCacheMappers @Inject constructor(
    private val moshi: Moshi,
) : HttpErrorCacheMappers<VodovozHttpError> {

    override fun String.toHttpError(): VodovozHttpError {
        return moshi.fromJson<VodovozHttpError>(this)
    }

}

interface HttpErrorCacheMappers<T : HttpError> {

    fun String.toHttpError(): T

}

@Keep
data class VodovozHttpError(
    @Json(name = "title")
    val title: String?,
    @Json(name = "message")
    val message: String?,
) : HttpError()

abstract class HttpError

interface HttpErrorCacheProvider {

    val httpErrorCache: HttpErrorCache

}

