package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.ProductCommentsDTO
import com.vodovoz.app.data.vodovoz_service.model.SORT_DTO
import com.vodovoz.app.data.vodovoz_service.model.WaitFeedbackProductDTO
import com.vodovoz.app.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.vodovoz.app.domain.general.model.ProductCommentsInfoModel
import com.vodovoz.app.domain.general.model.SectionModel
import com.vodovoz.app.domain.general.model.SortModel
import com.vodovoz.app.domain.general.model.WaitFeedbackProductModel

fun ProductCommentsDTO.toDomain(): ProductCommentsInfoModel {
    return ProductCommentsInfoModel(
        sorting = SORTIROVKA?.mapNotNull { sortDto -> sortDto?.toDomain() } ?: emptyList(),
        ratingText = RAITINGOSNOVA ?: "",
        commentsCount = COMMENT_COUNT ?: 0,
        commentsCountText = COMMENT_COUNT_TEXT ?: ""
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
