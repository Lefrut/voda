package com.vodovoz.app.domain.general.model.notification_settings

data class NotificationSectionItemModel(
    val id: Int,
    val message: String,
    val code: String,
    val type: String,
    val readOnly: Boolean,
    val value: String,
    val isVisible: Boolean
)
