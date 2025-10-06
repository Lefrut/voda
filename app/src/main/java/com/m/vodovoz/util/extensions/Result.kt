package com.m.vodovoz.util.extensions

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

fun <T> Flow<Result<T>>.catchResult(): Flow<Result<T>> = catch { throwable ->
    emit(Result.failure(throwable))
}

fun <T> resultFailure(throwable: Throwable = Throwable()) = Result.failure<T>(throwable)

suspend fun <T> Flow<Result<T>>.singleResult() =
    firstOrNull() ?: resultFailure(NoSuchElementException("Result flow is empty"))

suspend fun <T> Flow<Result<T>>.singleGetOrNull() =
    firstOrNull()?.getOrNull()



suspend fun <T> Flow<Result<T>>.deferredResult(): Deferred<Result<T>> {
    return coroutineScope {
        return@coroutineScope async {
            this@deferredResult.singleResult()
        }
    }
}

suspend fun <T> Deferred<Result<T>>.awaitOrNull(): T? {
    return await().getOrNull()
}

suspend inline fun <R> Flow<Result<R>>.firstResult(
) = runCatching {
    first().getOrThrow()
}


suspend inline fun <T, R> handleResultFlow(
    request: () -> Flow<Result<T>>,
    transform: T.() -> R,
    success: (R) -> Unit,
    failure: (Throwable) -> Unit,
) {
    request().singleResult().onSuccess { data ->
        success(transform(data))
    }.onFailure { throwable ->
        failure(throwable)
    }
}


fun <T> Flow<Result<T>>.onSuccess(action: suspend (T) -> Unit): Flow<Result<T>> = onEach { r ->
    r.onSuccess { action(it) }
}

fun <T> Flow<Result<T>>.onFailure(action: suspend (Throwable) -> Unit): Flow<Result<T>> = onEach { r ->
    r.onFailure { action(it) }
}