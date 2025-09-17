package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.ProductCommentsDTO
import com.m.vodovoz.data.vodovoz_service.model.SORT_DTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductDTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.m.vodovoz.domain.general.model.product.ProductCommentsInfoModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.product.SortModel
import com.m.vodovoz.domain.general.model.product.WaitFeedbackProductModel

fun ProductCommentsDTO.toDomain(): ProductCommentsInfoModel {
    return ProductCommentsInfoModel(
        sorting = SORTIROVKA?.mapNotNull { sortDto -> sortDto?.toDomain() } ?: emptyList(),
        ratingText = RAITINGOSNOVA ?: "",
        commentsCount = COMMENT_COUNT ?: 0,
        commentsCountText = COMMENT_COUNT_TEXT ?: "",
        images = IMAGES?.map { s ->
            s.toVodovozUrl()
        } ?: emptyList()
    )
}

fun SORT_DTO.toDomain(): SortModel? {
    return SortModel(
        name = NAME ?: return null,
        value = ZNACHIE ?: return null,
        order = SORT ?: return null
    )
}

fun WaitFeedbackProductsDTO.toDomain(): SectionModel<WaitFeedbackProductModel> {
    return SectionModel(
        title = title ?: "",
        items = products?.mapToDomain()?.ifEmpty {
            throw IllegalArgumentException("<WaitFeedbackProducts cant 'be null>")
        } ?: throw IllegalArgumentException("<WaitFeedbackProducts cant 'be null>"),
        button = null
    )
}

fun List<WaitFeedbackProductDTO>.mapToDomain(): List<WaitFeedbackProductModel> {
    return mapNotNull { it.toDomain() }
}

fun WaitFeedbackProductDTO.toDomain(): WaitFeedbackProductModel? {
    return WaitFeedbackProductModel(
        id = id ?: return null,
        name = name ?: return null,
        image = image?.toVodovozUrl() ?: return null
    )
}
