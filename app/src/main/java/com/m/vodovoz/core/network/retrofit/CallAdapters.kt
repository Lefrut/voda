package com.m.vodovoz.core.network.retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

class NoOpCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *> {

        return object : CallAdapter<Any?, Any?> {
            override fun responseType(): Type {
                return Any::class.java
            }

            override fun adapt(call: Call<Any?>): Any {
                return call
            }
        }

    }

    companion object {
        fun create(): CallAdapter.Factory {
            return NoOpCallAdapterFactory()
        }
    }
}
