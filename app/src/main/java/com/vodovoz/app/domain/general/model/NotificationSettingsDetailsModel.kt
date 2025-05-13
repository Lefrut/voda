package com.vodovoz.app.domain.general.model

data class NotificationSettingsDetailsModel(
    val title: String,
    val phoneTitle: String,
    val phoneField: FieldModel,
    val switchSections: List<SwitchSectionModel>
)
