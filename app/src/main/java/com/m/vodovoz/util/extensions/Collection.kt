package com.m.vodovoz.util.extensions

fun <E> Collection<E>.indexOfOrNull(element: @UnsafeVariance E?): Int? {
    return try {
        indexOf(element)
    } catch (ex: Exception) {
        null
    }
}

fun <E> Collection<E>.indexOfOrZero(element: @UnsafeVariance E?): Int {
    return try {
        indexOf(element)
    } catch (ex: Exception) { 0 }
}