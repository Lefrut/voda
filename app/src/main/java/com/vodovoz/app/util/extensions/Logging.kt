package com.vodovoz.app.util.extensions

import com.vodovoz.app.BuildConfig
import timber.log.Timber

inline fun <T : Any> debugLog(message: () -> T) {
    if (BuildConfig.DEBUG) {
        Timber.d(message.invoke().toString())
    }
}

inline fun <T : Any> debugLog(tag: String, message: () -> T) {
    if (BuildConfig.DEBUG) {
        Timber.d("$tag: " + message.invoke().toString())
    }
}