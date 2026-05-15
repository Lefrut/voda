package com.m.vodovoz.core.analytics

import com.m.vodovoz.BuildConfig
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.ecommerce.ECommerceEvent
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

data object Analytics :
    AnalyticsEvents by (if (!BuildConfig.DEBUG) AnalyticsImpl else FakeAnalytics)


interface AnalyticsEvents {

    fun setUserInstance(userId: StateFlow<Long?>) = Unit

    fun reportEvent(name: String, block: EventParamsBuilder.() -> Unit = {}) = Unit

    fun reportError(text: String, throwable: Throwable? = null) = Unit

    fun reportEcommerce(event: ECommerceEvent) = Unit
}


private data object AnalyticsImpl : AnalyticsEvents {
    private lateinit var userIdState: StateFlow<Long?>
    override fun setUserInstance(userId: StateFlow<Long?>) = synchronized(this) {
        if (::userIdState.isInitialized) return
        userIdState = userId
    }

    override fun reportEvent(
        name: String,
        block: EventParamsBuilder.() -> Unit
    ) {
        runCatching {
            val userId = userIdState.value ?: "0"
            val builder = EventParamsBuilder().apply(block)
            val params = builder.build().toMutableMap().apply {
                put("user_id", userId)
            }
            AppMetrica.reportEvent(name, JSONObject(params).toString())
        }
    }

    override fun reportError(text: String, throwable: Throwable?) {
        AppMetrica.reportError(text, throwable)
    }

    override fun reportEcommerce(event: ECommerceEvent) {
        AppMetrica.reportECommerce(event)
    }


}

data object FakeAnalytics : AnalyticsEvents

class EventParamsBuilder {
    private val map = mutableMapOf<String, Any?>()

    fun param(key: String, value: Any?) {
        map[key] = value
    }

    fun build(): Map<String, Any?> = map
}

private const val CURRENCY_RUB = "RUB"
