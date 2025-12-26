package com.m.vodovoz.core.analytics

import androidx.annotation.Keep
import com.m.vodovoz.BuildConfig
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data object Analytics {

    private val lock = Any()
    private lateinit var userIdState: StateFlow<Long?>


    fun setUserInstance(userId: StateFlow<Long?>) = synchronized(lock) {
        if (::userIdState.isInitialized) return
        userIdState = userId
    }

    @Keep
    fun reportEvent(text: String, eventParam: String? = null) = runCatching {
        if (!BuildConfig.DEBUG) {
            val userId = userIdState.value ?: "0"

            val eventParameters = buildString {
                append("""{"UserID":"$userId"""")
                if (eventParam != null) append(",").append(eventParam)
                append("}")
            }

            AppMetrica.reportEvent(text, eventParameters)
        }
    }

    @Keep
    fun reportError(text: String, throwable: Throwable? = null) = runCatching {
        if (!BuildConfig.DEBUG) {
            AppMetrica.reportError(text, throwable)
        }
    }

}