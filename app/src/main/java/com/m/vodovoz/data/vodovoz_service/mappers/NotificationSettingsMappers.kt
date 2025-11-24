package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.equalsTo
import com.m.vodovoz.data.vodovoz_service.model.notification_settings.NOTIFICATION_SECTION_DTO
import com.m.vodovoz.data.vodovoz_service.model.notification_settings.NOTIFICATION_SECTION_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.notification_settings.NotificationSettingsDetailsDTO
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.user.NotificationSectionItemModel
import com.m.vodovoz.domain.general.model.user.NotificationSettingsDetailsModel

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
    return NotificationSectionItemModel(
        id = ID ?: -1,
        message = MESSAGE ?: "",
        code = KLYCH ?: return null,
        type = FIELD_TYPE ?: "",
        readOnly = VodovozBoolean.True equalsTo ZAPRETREDAK,
        value = VALUE ?: "",
        isVisible = FIELD_TYPE != "hidden"
    )
}

fun List<NOTIFICATION_SECTION_ITEM_DTO>.mapToDomain(): List<NotificationSectionItemModel> {
    return mapNotNull { it.toDomain() }
}




