package com.m.vodovoz.feature.main

import androidx.compose.ui.unit.Dp
import androidx.fragment.app.Fragment
import kotlin.math.roundToInt

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

fun Fragment.setFloatingPromoExtraBottomOffset(offset: Dp) {
    val offsetPx = (offset.value * resources.displayMetrics.density).roundToInt()
    setFloatingPromoExtraBottomOffset(offsetPx)
}

fun Fragment.clearFloatingPromoExtraBottomOffset() {
    floatingPromoUiHost()?.setFloatingPromoExtraBottomOffset(null)
}

fun Fragment.setFloatingPromoSuppressed(suppressed: Boolean) {
    floatingPromoUiHost()?.setFloatingPromoSuppressed(suppressed)
}
