package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.BrandDTO
import com.m.vodovoz.data.vodovoz_service.model.BrandSectionDTO
import com.m.vodovoz.domain.general.model.promotion.BrandModel
import com.m.vodovoz.domain.general.model.promotion.BrandSectionModel

fun BrandSectionDTO.toDomain(): BrandSectionModel {
    return BrandSectionModel(
        title = TITLE ?: "",
        count = COUNT ?: "",
        brands = DATA?.mapToDomain() ?: emptyList()
    )
}

fun List<BrandDTO>.mapToDomain(): List<BrandModel>{
    return mapNotNull { it.toDomain() }
}

fun BrandDTO.toDomain(): BrandModel? {
    return BrandModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        picture = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        pageUrl = DETAIL_PAGE_URL ?: ""
    )
}