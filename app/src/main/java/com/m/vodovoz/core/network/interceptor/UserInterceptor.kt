package com.m.vodovoz.core.network.interceptor

import com.m.vodovoz.common.account.AccountManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInterceptor @Inject constructor(
    private val accountManager: AccountManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val userId = accountManager.fetchAccountId()

        val newUrl = if (userId != null) originalUrl.newBuilder()
            .addQueryParameter("userid", userId.toString())
            .build()
        else originalUrl

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }
}