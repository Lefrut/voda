package com.m.vodovoz.common.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

abstract class HttpErrorCache(
    private val mappers: HttpErrorCacheMappers<*>,
) {
    private val _lastErrorData: MutableStateFlow<String?> = MutableStateFlow(null)

    val lastHttpError: StateFlow<HttpError?> = _lastErrorData.map { s ->
        with(mappers) { s?.toHttpError() }
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun setLastError(raw: String?) {
        _lastErrorData.value = raw
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

