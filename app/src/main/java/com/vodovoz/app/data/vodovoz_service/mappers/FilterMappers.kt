package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.filters.FilterBoundsDTO
import com.vodovoz.app.data.vodovoz_service.model.filters.FilterDTO
import com.vodovoz.app.data.vodovoz_service.model.filters.FilterValueDTO
import com.vodovoz.app.data.vodovoz_service.model.filters.FilterValuesDTO
import com.vodovoz.app.data.vodovoz_service.model.filters.FiltersDTO
import com.vodovoz.app.domain.general.model.FilterModel
import com.vodovoz.app.domain.general.model.FilterValueModel
import com.vodovoz.app.domain.general.model.FiltersModel
import kotlin.math.max
import kotlin.math.min

fun FiltersDTO.toDomain(): FiltersModel {
    return FiltersModel(
        priceRange = CENAFILTER?.toDomain() ?: throw IllegalArgumentException("Max of filter price can't be null"),
        filters = DANNIE?.mapToDomain() ?: throw IllegalArgumentException("Filters can't be null")
    )
}

fun List<FilterDTO>.mapToDomain(): List<FilterModel> {
    return mapNotNull { it.toDomain() }
}

fun FilterDTO.toDomain(): FilterModel? {
    val bounds = ZHACFILTER?.toDomain()
    return FilterModel(
        id = CODE ?: return null,
        name = NAME ?: return null,
        totalValues = ZNACHEIE?.COUNT ?: 0,
        values = ZNACHEIE?.toDomain() ?: emptyList(),
        bounds = bounds,
        currentBounds = bounds

    )
}

fun FilterBoundsDTO.toDomain(): IntRange?{
    val min = MIN ?: return null
    val max = max(
        min, MAX ?: return null
    )
    return min(min, max)..max
}

@JvmName("mapToListFilterValueModel")
fun List<FilterValueDTO>.mapToDomain(): List<FilterValueModel> {
    return mapNotNull {
        FilterValueModel(
            id = it.ID ?: return@mapNotNull null,
            value = it.VALUE ?: return@mapNotNull null
        )
    }
}

fun FilterValuesDTO.toDomain(): List<FilterValueModel> {
    return DATA?.map {
        FilterValueModel(it, it)
    } ?: emptyList()
}