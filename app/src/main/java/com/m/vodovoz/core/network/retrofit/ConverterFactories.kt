package com.m.vodovoz.core.network.retrofit

import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

 class NoOpConverterFactory  private constructor(
     private val moshi: Moshi
 ) : Converter.Factory() {

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
         retrofit: Retrofit
     ): Converter<Any?, RequestBody?> {
         val adapter = moshi.adapter<Any>(type)
         return Converter { value ->
             val json = adapter.toJson(value)
             json.toRequestBody("application/json".toMediaType())
         }
     }

    companion object {

        fun create(moshi: Moshi): Converter.Factory {
            return NoOpConverterFactory(moshi)
        }
    }
}
