package com.vodovoz.app.core.network.retrofit

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NoOpConverterFactory private constructor(): Converter.Factory() {

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>, retrofit: Retrofit,
    ): Converter<ResponseBody, *> {
        return Converter<ResponseBody, Any?> { value ->
            value.string()
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<Any?, RequestBody?> {
        return Converter<Any?, RequestBody?> {
            null
        }
    }

    companion object {
        fun create(): NoOpConverterFactory {
            return NoOpConverterFactory()
        }
    }
}
