package com.vodovoz.app.domain.general.model

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
        val currentBounds = filter.currentBounds
        val data = if (currentBounds?.start != null) {
            listOf(currentBounds.start, currentBounds.endInclusive).joinToString(",")
        } else filter.values.joinToString(",") { it.id }

        "${filter.id}@${data}"
    }
}

fun List<FilterModel>.toSliderQueries(): Map<String,String?> {
    return filter { it.currentBounds != it.bounds && it.currentBounds != null }
        .flatMap { filter ->
            listOf(
                "${filter.id}_from" to filter.currentBounds?.start?.toString(),
                "${filter.id}_to" to filter.currentBounds?.endInclusive?.toString()
            )
        }
        .toMap()
}