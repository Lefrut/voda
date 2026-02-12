package com.m.vodovoz.util.extensions

fun <E> Collection<E>.indexOfOrNull(element: E?): Int? {
    val index = indexOf(element)
    return if (index >= 0) index else null
}

fun <E> Collection<E>.indexOfOrZero(element: @UnsafeVariance E?): Int {
    val index = indexOf(element)
    return if (index >= 0) index else 0
}