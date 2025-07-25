package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.ACTION_DTO
import com.vodovoz.app.data.vodovoz_service.model.COLORFUL_KNOPKA_DTO
import com.vodovoz.app.data.vodovoz_service.model.STORY_DTO
import com.vodovoz.app.data.vodovoz_service.model.StoriesDTO
import com.vodovoz.app.data.vodovoz_service.model.VNYTRENNOST_DTO
import com.vodovoz.app.domain.general.model.promotion.ActionWithButtonModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.promotion.StoryModel


fun StoriesDTO.toDomain(): List<StoryModel> {
    return data?.mapNotNull { it.toDomain() } ?: emptyList()
}

fun STORY_DTO.toDomain(): StoryModel? {
    return StoryModel(
        id = ID ?: return null,
        previewImage = RAZDEL?.IMAGE?.toVodovozUrl() ?: "",
        pages = VNYTRENNOST?.mapNotNull { it?.toDomain() } ?: return null,
        viewed = false
    )
}

fun VNYTRENNOST_DTO.toDomain(): ActionWithButtonModel? {
    return ActionWithButtonModel(
        action = ACTION_DTO(ACTION, ID).toAction(73) ?: return null,
        colorfulButton = KNOPKA?.toDomain() ?: return null,
        image = IMAGE?.toVodovozUrl() ?: ""
    )
}

fun List<COLORFUL_KNOPKA_DTO>.mapToDomain(): List<ColorfulButtonModel> {
    return map { it.toDomain() }
}

fun COLORFUL_KNOPKA_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = NAME ?: TEXT ?: "",
        backgroundColor = COLOR_BACKGROUND ?: BACKGROUND ?: "",
        textColor = COLOR_TEXT ?: COLOR ?: "",
        id = ID ?: ""
    )
}