package com.m.vodovoz.core.network.interceptor

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseUrlInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var newBaseUrl: HttpUrl? = null

    fun updateBaseUrl(url: String) {
        newBaseUrl = url.toHttpUrlOrNull()
    }

    fun clear() {
        newBaseUrl = null
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val baseUrl = newBaseUrl ?: return chain.proceed(request)

        val newUrl = with(request.url){
            baseUrl.newBuilder()
                .addEncodedPathSegments(encodedPath.substring(1))
                .encodedQuery(encodedQuery)
                .build()
        }

        val newRequest = request.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}