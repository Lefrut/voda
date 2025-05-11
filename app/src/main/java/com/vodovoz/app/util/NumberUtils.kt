package com.vodovoz.app.util

import java.math.BigDecimal

fun String.toExactIntOrNull(): Int? = try {
    BigDecimal(trim().replace(',', '.'))
        .stripTrailingZeros()
        .intValueExact()
} catch (e: Throwable) {
    null
}