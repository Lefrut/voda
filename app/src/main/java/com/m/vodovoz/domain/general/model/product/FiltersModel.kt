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
        currentBounds != null && bounds?.isCloseToStrict(currentBounds) == false
    }
        .flatMap { filter ->
            listOf(
                "${filter.id}_from" to filter.currentBounds?.start?.toString(),
                "${filter.id}_to" to filter.currentBounds?.endInclusive?.toString()
            )
        }
        .toMap()
}

fun ClosedRange<Float>.isCloseToStrict(
    other: ClosedRange<Float>,
    epsilon: Float = 0.02f
): Boolean {
    val startDiff = kotlin.math.abs(this.start - other.start)
    val endDiff = kotlin.math.abs(this.endInclusive - other.endInclusive)

    return startDiff <= epsilon && endDiff <= epsilon
}