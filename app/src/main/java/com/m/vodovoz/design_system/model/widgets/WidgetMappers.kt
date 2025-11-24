package com.m.vodovoz.design_system.model.widgets

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.equalsTo
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.user.NotificationSectionItemModel

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
            isVisible = isVisible
        ).toUi()
    }
}