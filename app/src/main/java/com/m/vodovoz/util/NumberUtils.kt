package com.m.vodovoz.util

import java.math.BigDecimal
import java.math.RoundingMode

fun String.toExactIntOrNull(): Int? = try {
    BigDecimal(trim().replace(',', '.'))
        .stripTrailingZeros()
        .intValueExact()
} catch (e: Throwable) {
    null
}



fun Float.clearZeros(): String = try {
    BigDecimal(toString()).stripTrailingZeros().toString()
} catch (e: Throwable) {
    toString()
}


fun String.toRoundIntOrNull(): Int? = try {
    BigDecimal(trim().replace(',', '.')).setScale(0, RoundingMode.HALF_UP).toInt()
} catch (e: Throwable) {
    null
}

fun Float.roundToOneDecimal(): Float {
    return toBigDecimal()
        .setScale(1, RoundingMode.HALF_UP)
        .toFloat()
}

fun String.smartParseFloat(): Float? {
    if (isBlank()) return null

    return formatNumber().toFloatOrNull()
}

fun String.formatNumber(): String {
    val cleaned = trim()
        .replace(Regex("[^0-9,.-]"), "")
        .replace(",", ".")
        .let { s ->
            val parts = s.split(".")
            if (parts.size > 2) {
                parts.dropLast(1).joinToString("").plus(".").plus(parts.last())
            } else s
        }
    if (cleaned.matches(Regex("""\d+\."""))) return cleaned

    return try {
        BigDecimal(cleaned).stripTrailingZeros().toPlainString()
    } catch (e: NumberFormatException) { cleaned }
}

fun String.isTrailingDotOnly(): Boolean {
    val trimmed = trim().replace(",", ".")
    return trimmed.matches(Regex("""\d+\."""))
}