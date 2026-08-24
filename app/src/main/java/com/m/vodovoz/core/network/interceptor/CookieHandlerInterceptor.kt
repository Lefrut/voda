package com.m.vodovoz.core.network.interceptor

import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.util.extensions.debugLog
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieHandlerInterceptor @Inject constructor(
    private val cookieManager: CookieManager,
    private val accountManager: AccountManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        debugLog { "LogSettings.NETWORK_LOG: $originalRequest" }

        val builder = originalRequest.newBuilder()
        val explicitCookie = originalRequest.header(COOKIE_HEADER)
        val storedSessionCookie = cookieManager.fetchCookieSessionId()

        when {
            explicitCookie != null && explicitCookie.isBlank() -> {
                // An empty Cookie header marks requests (for example relogin) that must not use
                // the current PHP session.
                builder.removeHeader(COOKIE_HEADER)
            }

            explicitCookie == null -> {
                storedSessionCookie?.let { cookieSessionId ->
                    builder.header(COOKIE_HEADER, cookieSessionId)
                }
            }
        }

        val request = builder.build()

        debugLog {
            "Cookie sent to server: ${request.header(COOKIE_HEADER)}"
        }

        val originalResponse = chain.proceed(request)

        if (!accountManager.isAlreadyLogin()) {
            val guestCookieNeedsUpdate = storedSessionCookie == null || cookieManager.isOldCookie()
            if (guestCookieNeedsUpdate) {
                originalResponse.headers.values(SET_COOKIE_HEADER)
                    .firstOrNull { cookie -> cookie.startsWith(PHP_SESSION_PREFIX) }
                    ?.let(cookieManager::updateCookieSessionId)
            }
        }

        return originalResponse
    }

    private companion object {
        const val COOKIE_HEADER = "Cookie"
        const val SET_COOKIE_HEADER = "Set-Cookie"
        const val PHP_SESSION_PREFIX = "PHPSESSID="
    }
}
