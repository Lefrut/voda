package com.vodovoz.app.util

import java.math.BigDecimal
import java.math.RoundingMode

fun String.toExactIntOrNull(): Int? = try {
    BigDecimal(trim().replace(',', '.'))
        .stripTrailingZeros()
        .intValueExact()
} catch (e: Throwable) {
    null
}

fun String.toIntRoundOrNull(): Int? = try {
    BigDecimal(trim().replace(',', '.')).setScale(0, RoundingMode.HALF_UP).toInt()
} catch (e: Throwable) {
    null
}