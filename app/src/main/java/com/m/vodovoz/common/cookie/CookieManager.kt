package com.m.vodovoz.common.cookie

import com.m.vodovoz.common.datastore.DataStorePrefs
import com.m.vodovoz.util.extensions.debugLog
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookieManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
) {

    fun fetchCookieSessionId() = runBlocking {
        dataStorePrefs.getString(COOKIE_SESSION_ID)?.toSessionCookie()
    }

    fun updateCookieSessionId(cookieSessionId: String?) {
        cookieSessionId?.toSessionCookie()?.let { sessionCookie ->
            dataStorePrefs.putString(COOKIE_SESSION_ID, sessionCookie)
            debugLog { "Session cookie updated" }
            setLastEntire()
        }
    }

    fun removeCookieSessionId() {
        dataStorePrefs.remove(COOKIE_SESSION_ID)
        dataStorePrefs.remove(COOKIE_LAST_ENTIRE)
    }

    fun isOldCookie(): Boolean {
        val currentEntire = System.currentTimeMillis()
        val lastEntire = dataStorePrefs.getLong(COOKIE_LAST_ENTIRE) ?: 0
        val diff = currentEntire - lastEntire
        return diff > COOKIES_LIFE_TIME_IN_MILLIS
    }

    private fun setLastEntire() {
        dataStorePrefs.putLong(COOKIE_LAST_ENTIRE, System.currentTimeMillis())
    }

    private fun String.toSessionCookie(): String? {
        return substringBefore(';')
            .trim()
            .takeIf { cookie -> cookie.startsWith(PHP_SESSION_PREFIX) }
    }

    companion object {
        //Cookie Settings
        private const val COOKIE_SESSION_ID = "cookies"
        private const val COOKIE_LAST_ENTIRE = "last_entire"
        private const val PHP_SESSION_PREFIX = "PHPSESSID="
        private const val COOKIES_LIFE_TIME_IN_MIN = 50
        private const val COOKIES_LIFE_TIME_IN_MILLIS = COOKIES_LIFE_TIME_IN_MIN * 60 * 1000
    }

}
