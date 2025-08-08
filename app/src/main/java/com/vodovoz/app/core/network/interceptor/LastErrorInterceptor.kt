package com.vodovoz.app.core.network.interceptor

import com.vodovoz.app.common.cache.HttpError
import com.vodovoz.app.common.cache.HttpErrorCache
import com.vodovoz.app.common.cache.VodovozHttpErrorCache
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastErrorInterceptor @Inject constructor(
    private val errorCache: HttpErrorCache,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (!response.isSuccessful) {
            val responseBody = response.body
            val content = responseBody?.string()

            errorCache.setLastError(content)

            val newBody = content?.toResponseBody(
                responseBody.contentType()
            )

            return response.newBuilder()
                .body(newBody)
                .build()
        }

        return response
    }

}