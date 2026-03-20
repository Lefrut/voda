package com.m.vodovoz.ui.graphics

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun Color.Companion.fromHexOrUnspecified(hexString: String) = try {
    Color(hexString.toColorInt())
} catch (_: Exception) {
    Unspecified
}

fun Color.Companion.fromHexOrNull(hexString: String) = try {
    Color(hexString.toColorInt())
} catch (_: Exception) {
    null
}
