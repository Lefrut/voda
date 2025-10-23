package com.m.vodovoz.domain.general.model.product

data class FiltersModel(
    val priceRange: IntRange,
    val filters: List<FilterModel>,
)

data class FilterModel(
    val id: String,
    val name: String,
    val totalValues: Int,
    val values: List<FilterValueModel>,
    val bounds: ClosedRange<Float>?,
    val currentBounds: ClosedRange<Float>?,
)

data class FilterValueModel(
    val id: String,
    val value: String,
)

fun List<FilterModel>.format(): String {
    return joinToString(";") { filter ->
        val data = filter.values.joinToString(",") { it.id }
        "${filter.id}@${data}"
    }
}

fun List<FilterModel>.toSliderQueries(): Map<String, String?> {
    return filter { it ->
        val bounds = it.bounds
        val currentBounds = it.currentBounds
        currentBounds != null && bounds?.isCloseTo(currentBounds) != true
    }
        .flatMap { filter ->
            listOf(
                "${filter.id}_from" to filter.currentBounds?.start?.toString(),
                "${filter.id}_to" to filter.currentBounds?.endInclusive?.toString()
            )
        }
        .toMap()
}

private fun ClosedRange<Float>.isCloseTo(
    other: ClosedRange<Float>,
    epsilon: Float = 0.02f
): Boolean {
    val thisStart = start - epsilon
    val thisEnd = endInclusive + epsilon
    val otherStart = other.start - epsilon
    val otherEnd = other.endInclusive + epsilon
    return thisStart <= otherEnd && otherStart <= thisEnd
}
