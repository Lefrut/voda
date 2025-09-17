package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.BannerDTO
import com.m.vodovoz.domain.general.model.promotion.BannerModel


fun List<BannerDTO>.mapToDomain(): List<BannerModel> {
    return mapNotNull { it.toDomain() }
}

fun BannerDTO.toDomain(): BannerModel? {
    return BannerModel(
        id = ID ?: return null,
        name = NAME ?: "",
        detailPicture = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        action = HARAKTERISTIK?.toAction(IBLOCK_ID ?: 73L) ?: return null,
        advertising = OREKLAME?.toDomain()
    )
}