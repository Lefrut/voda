package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.user_data.FOTO_DTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.UserDataDTO
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.widgets.FieldOptionModel
import com.m.vodovoz.domain.general.model.user.UserDataModel
import com.m.vodovoz.domain.general.model.user.UserDataPhotoModel

fun UserDataDTO.toDomain(): UserDataModel {
    return UserDataModel(
        title = TITLE ?: "",
        photo = FOTO?.toDomain() ?: throw IllegalArgumentException("UserDataPhoto can't be null"),
        fields = POLYA?.mapToDomain()
            ?: throw IllegalArgumentException("UserDataFields can't be null"),
        deleteTextPrefix = TEXT?.NAME ?: "",
        deleteText = TEXT?.TEXTID ?: ""
    )
}


fun FOTO_DTO.toDomain(): UserDataPhotoModel {
    return UserDataPhotoModel(
        imageUrl = IMG?.toVodovozUrl() ?: "",
        title = TITLE ?: "",
        description = OPISANIE ?: ""
    )
}

fun List<POLE_DTO>.mapToDomain(): List<FieldModel> {
    return mapNotNull { it.toDomain() }
}

fun POLE_DTO.toDomain(): FieldModel? {
    return FieldModel(
        id = CODE ?: ID ?: PROP_CODE ?: return null,
        label = NAME ?: TEXT ?: "",
        value = VALUE ?: "",
        valueType = POLE ?: "text",
        isRequired = (OBYZATELNO ?: OBAZATELEN) == "Y",
        readOnly = ZABLOCKPOLE == "Y",
        supportingText = OPIS ?: "",
        hint = TEXTOPIS ?: TEXT_V_POLE ?: TEXTVPOLE ?: "",
        values = SPISOK?.mapNotNull { item ->
            FieldOptionModel(
                item.ID ?: return@mapNotNull null,
                item.NAME ?: return@mapNotNull null
            )
        } ?: emptyList()
    )
}