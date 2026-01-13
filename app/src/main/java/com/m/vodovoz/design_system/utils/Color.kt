package com.m.vodovoz.design_system.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

fun Color.toHexString(includeAlpha: Boolean = false): String = try {
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)
    val a = (alpha * 255).roundToInt().coerceIn(0, 255)

    if (includeAlpha) {
        "#%02X%02X%02X%02X".format(a, r, g, b)
    } else {
        "#%02X%02X%02X".format(r, g, b)
    }
} catch (_: Throwable) {
    ""
}
