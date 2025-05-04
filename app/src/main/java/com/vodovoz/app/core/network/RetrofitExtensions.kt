package com.vodovoz.app.core.network

import retrofit2.Response


fun <T> Response<T>.messageWithCode(): String {
    return "${message()} - ${code()}"
}

fun <T> Response<T>.stringBody(): String {
    return try {
        ((body() as? String) ?: (errorBody() ?: raw().body)?.string()) ?: ""
    } catch (_: Throwable) { "" }
}
