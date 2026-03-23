package com.m.vodovoz.core.analytics

import androidx.annotation.Keep
import com.m.vodovoz.BuildConfig
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

data object Analytics {

    private val lock = Any()
    private lateinit var userIdState: StateFlow<Long?>


    fun setUserInstance(userId: StateFlow<Long?>) = synchronized(lock) {
        if (::userIdState.isInitialized) return
        userIdState = userId
    }

    @Keep
    fun reportEvent(name: String, block: EventParamsBuilder.() -> Unit = {}) = runCatching {
        if (!BuildConfig.DEBUG) {
            val userId = userIdState.value ?: "0"
            val builder = EventParamsBuilder().apply(block)
            val params = builder.build().toMutableMap().apply {
                put("user_id", userId)
            }
            AppMetrica.reportEvent(name, JSONObject(params).toString())
        }
    }


    @Keep
    fun reportError(text: String, throwable: Throwable? = null) = runCatching {
        if (!BuildConfig.DEBUG) {
            AppMetrica.reportError(text, throwable)
        }
    }

}

class EventParamsBuilder {
    private val map = mutableMapOf<String, Any?>()

    fun param(key: String, value: Any?) {
        map[key] = value
    }

    fun build(): Map<String, Any?> = map
}

