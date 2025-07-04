package com.vodovoz.app.core.network.retrofit

import retrofit2.Response


fun <T> Response<T>.messageWithCode(): String {
    return "${message()} - ${code()}"
}

fun <T> Response<T>.stringBody(): String {
    return try {
        (body() as? String) ?: ""
    } catch (_: Throwable) { "" }
}


fun <T> Response<T>.stringErrorBody(): String {
    return errorBody()?.string() ?: ""
}

