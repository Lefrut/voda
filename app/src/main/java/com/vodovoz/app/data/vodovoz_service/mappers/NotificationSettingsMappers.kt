package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.equalsTo
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.NOTIFICATION_SECTION_DTO
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.NOTIFICATION_SECTION_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.NotificationSettingsDetailsDTO
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.user.NotificationSectionItemModel
import com.vodovoz.app.domain.general.model.user.NotificationSettingsDetailsModel

fun NotificationSettingsDetailsDTO.toDomain(): NotificationSettingsDetailsModel {
    return NotificationSettingsDetailsModel(
        title = TEXT ?: "",
        sections = DANNYE?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain() ?: ColorfulButtonModel.Empty
    )
}


@JvmName("mapToNotificationSectionItemModelSectionModelList")
fun List<NOTIFICATION_SECTION_DTO>.mapToDomain(): List<SectionModel<NotificationSectionItemModel>> {
    return mapNotNull { notificationSectionDto ->
        notificationSectionDto.toDomain()
    }
}

fun NOTIFICATION_SECTION_DTO.toDomain(): SectionModel<NotificationSectionItemModel>? {
    return SectionModel(
        title = NAME ?: "",
        items = POLA?.mapToDomain()?.ifEmpty { return null } ?: return null,
        button = null
    )
}

fun NOTIFICATION_SECTION_ITEM_DTO.toDomain(): NotificationSectionItemModel? {
    if(FIELD_TYPE == "hidden") return null
    return NotificationSectionItemModel(
        id = ID ?: -1,
        message = MESSAGE ?: "",
        code = KLYCH ?: return null,
        type = FIELD_TYPE ?: "",
        readOnly = VodovozBoolean.True equalsTo ZAPRETREDAK,
        value = VALUE ?: "",
        isVisible = true
    )
}

fun List<NOTIFICATION_SECTION_ITEM_DTO>.mapToDomain(): List<NotificationSectionItemModel> {
    return mapNotNull { it.toDomain() }
}




