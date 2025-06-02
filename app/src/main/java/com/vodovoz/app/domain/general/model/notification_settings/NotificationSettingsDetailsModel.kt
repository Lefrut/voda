package com.vodovoz.app.domain.general.model.notification_settings

import com.vodovoz.app.domain.general.model.ColorfulButtonModel
import com.vodovoz.app.domain.general.model.SectionModel

data class NotificationSettingsDetailsModel(
    val title: String,
    val sections: List<SectionModel<NotificationSectionItemModel>>,
    val button: ColorfulButtonModel
)
