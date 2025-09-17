package com.m.vodovoz.common.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

abstract class HttpErrorCache(
    mappers: HttpErrorCacheMappers<*>,
) {
    private val _lastErrorData: MutableStateFlow<String?> = MutableStateFlow(null)

    val lastHttpError: Flow<HttpError?> = _lastErrorData.map { s ->
        kotlin.runCatching { with(mappers) { s?.toHttpError() } }.getOrNull()
    }

    fun setLastError(errorData: String?) {
        _lastErrorData.update { errorData }

    }
}

abstract class HttpError

interface HttpErrorCacheMappers<T : HttpError> {

    fun String.toHttpError(): T

}


val emptyHttpErrorCacheMappers = object : HttpErrorCacheMappers<HttpError> {
    override fun String.toHttpError(): HttpError = object : HttpError() {}
}

val emptyHttpErrorCache = object : HttpErrorCache(emptyHttpErrorCacheMappers) {}

