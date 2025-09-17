package com.m.vodovoz.util.extensions

public fun <E> Collection<E>.indexOfOrNull(element: @UnsafeVariance E?): Int? {
    return try {
        indexOf(element)
    } catch (ex: Exception) {
        null
    }
}