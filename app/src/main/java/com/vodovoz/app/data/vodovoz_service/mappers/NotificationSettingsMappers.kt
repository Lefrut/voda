package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.data.vodovoz_service.model.notification_settings.NotificationSettingsDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.SWITCH_DTO
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.SWITCH_SECTION_DTO
import com.vodovoz.app.data.vodovoz_service.model.notification_settings.TELEFON_DTO
import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.NotificationSettingsDetailsModel
import com.vodovoz.app.domain.general.model.SwitchModel
import com.vodovoz.app.domain.general.model.SwitchSectionModel

fun NotificationSettingsDetailsDTO.toDomain(): NotificationSettingsDetailsModel {
    return NotificationSettingsDetailsModel(
        title = TITLE ?: "",
        phoneTitle = TELEFON?.NAME ?: "",
        phoneField = TELEFON?.toDomain()
            ?: throw IllegalArgumentException("Notification settings phone can't be null"),
        switchSections = LISTADATA?.mapToDomain()
            ?: throw IllegalArgumentException("Notification settings switches can't be null")
    )
}

fun TELEFON_DTO.toDomain(): FieldModel {
    return FieldModel(
        id = KLYCH ?: "phone",
        label = "",
        value = ACTIVE ?: "",
        valueType = "phone",
        isRequired = true,
        readOnly = false,
        supportingText = "",
        hint = ""
    )
}

fun List<SWITCH_SECTION_DTO>.mapToDomain(): List<SwitchSectionModel> {
    return mapNotNull { it.toDomain() }
}

fun SWITCH_SECTION_DTO.toDomain(): SwitchSectionModel? {
    return SwitchSectionModel(
        title = ZAGOLOVOK ?: "",
        description = OPISANIE ?: "",
        switches = DANNYE?.mapNotNull { it?.toDomain() }?.apply {
            if (isEmpty()) return null
        } ?: return null
    )
}


fun SWITCH_DTO.toDomain(): SwitchModel? {
    return SwitchModel(
        id = KLYCH ?: return null,
        name = NAME ?: return null,
        active = ACTIVE == "Y"
    )
}

