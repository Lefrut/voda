package com.m.vodovoz.feature.main

import androidx.fragment.app.Fragment

interface FloatingPromoUiHost {
    fun setFloatingPromoExtraBottomOffset(offsetPx: Int?)
    fun setFloatingPromoSuppressed(suppressed: Boolean)
}

private fun Fragment.floatingPromoUiHost(): FloatingPromoUiHost? {
    return generateSequence(parentFragment) { it.parentFragment }
        .filterIsInstance<FloatingPromoUiHost>()
        .firstOrNull()
}

fun Fragment.setFloatingPromoExtraBottomOffset(offsetPx: Int) {
    floatingPromoUiHost()?.setFloatingPromoExtraBottomOffset(offsetPx.coerceAtLeast(0))
}

fun Fragment.clearFloatingPromoExtraBottomOffset() {
    floatingPromoUiHost()?.setFloatingPromoExtraBottomOffset(null)
}

fun Fragment.setFloatingPromoSuppressed(suppressed: Boolean) {
    floatingPromoUiHost()?.setFloatingPromoSuppressed(suppressed)
}
