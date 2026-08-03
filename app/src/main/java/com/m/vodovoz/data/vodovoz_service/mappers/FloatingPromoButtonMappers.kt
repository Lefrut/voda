package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.FloatingPromoButtonDTO
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel

fun List<FloatingPromoButtonDTO>.mapToFloatingPromoButtons(): List<FloatingPromoButtonModel> {
    return mapNotNull(FloatingPromoButtonDTO::toDomain)
}

fun FloatingPromoButtonDTO.toDomain(): FloatingPromoButtonModel? {
    val action = HARAKTERISTIK?.ACTION?.takeIf(String::isNotBlank) ?: return null
    val actionId = HARAKTERISTIK.ID?.takeIf(String::isNotBlank) ?: return null

    return FloatingPromoButtonModel(
        id = ID ?: return null,
        name = NAME.orEmpty(),
        imageUrl = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        action = action,
        actionId = actionId,
        blockId = IBLOCK_ID?.toLong() ?: 0L,
        leftScreenNames = POKAZ?.LEFT.toScreenNames(),
        rightScreenNames = POKAZ?.RIGHT.orEmpty()
            .toScreenNames(),
    )
}

private fun List<String>?.toScreenNames(): Set<String> {
    return orEmpty()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toSet()
}
