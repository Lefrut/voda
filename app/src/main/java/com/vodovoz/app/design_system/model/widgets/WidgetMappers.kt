package com.vodovoz.app.design_system.model.widgets

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.equalsTo
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.SectionModel
import com.vodovoz.app.domain.general.model.notification_settings.NotificationSectionItemModel

fun SectionModel<NotificationSectionItemModel>.toUi(): SectionUi<WidgetUi> {
    return SectionUi(
        title = title,
        items = items.mapToUi(),
        button = null
    )
}

fun List<NotificationSectionItemModel>.mapToUi(): List<WidgetUi> {
    return mapNotNull { it.toUi() }
}

fun NotificationSectionItemModel.toUi(): WidgetUi? {
    return when {
        !isVisible -> return null
        type == "checkbox" -> SwitchUi(
            id = code,
            name = message,
            value = VodovozBoolean.True equalsTo value,
            enabled = !readOnly
        )

        else -> FieldModel(
            id = code,
            label = "",
            valueType = type,
            value = value,
            isRequired = true,
            readOnly = readOnly,
            supportingText = "",
            hint = "",
        ).toUi()
    }
}