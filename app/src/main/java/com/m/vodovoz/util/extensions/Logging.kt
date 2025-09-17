package com.m.vodovoz.util.extensions


import com.m.vodovoz.BuildConfig
import timber.log.Timber


inline fun <T : Any> debugLog(message: () -> T) {
    if (!BuildConfig.DEBUG) return

    val el = Throwable().stackTrace.getOrNull(1)
    val declaringClass = el?.className
        ?.substringAfterLast('.')?.replace("$", "():")
        ?: "UnknownClass"

    Timber.d("[$declaringClass] ${message()}")
}
