package com.vodovoz.app.core.network.retrofit

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response


fun <T> Response<T>.messageWithCode(): String {
    return "${message()} - ${code()}"
}

fun <T> Response<T>.stringBody(): String {
    return try {
        (body() as? String) ?: ""
    } catch (_: Throwable) {
        ""
    }
}


fun <T> Response<T>.stringErrorBody(): String {
    return errorBody()?.string() ?: ""
}

fun List<ByteArray>.prepareImageParts(name: String): List<MultipartBody.Part> {
    return mapIndexed { index, bytes ->
        val requestBody = bytes.toRequestBody("image/*".toMediaType())
        MultipartBody.Part.createFormData(
            name,
            "image_$index.jpg",
            requestBody
        )
    }
}

