package com.vodovoz.app.util.extensions

fun calculateActiveRange(
    min: Int,
    max: Int,
    currentMin: Int,
    currentMax: Int
): ClosedFloatingPointRange<Float> {
    val range = (max.toFloat() - min).takeIf { it != 0f } ?: return 0f..1f

    val start = (currentMin - min) / range
    val end = (currentMax - min) / range

    val normalizedStart = start.coerceIn(0f, 1f)
    val normalizedEnd = end.coerceIn(0f, 1f)

    return minOf(normalizedStart, normalizedEnd)..maxOf(normalizedStart, normalizedEnd)
}

fun calculateActiveRange(
    min: Float,
    max: Float,
    currentMin: Float,
    currentMax: Float
): ClosedFloatingPointRange<Float> {
    val totalRange = max - min
    if (totalRange <= 0f) return 0f..0f

    val normalizedStart = ((currentMin - min) / totalRange).coerceIn(0f, 1f)
    val normalizedEnd = ((currentMax - min) / totalRange).coerceIn(0f, 1f)

    return normalizedStart..normalizedEnd
}