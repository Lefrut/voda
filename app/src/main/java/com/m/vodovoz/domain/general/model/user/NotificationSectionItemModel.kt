package com.m.vodovoz.domain.general.model.user

data class NotificationSectionItemModel(
    val id: Int,
    val message: String,
    val code: String,
    val type: String,
    val readOnly: Boolean,
    val value: String,
    val isVisible: Boolean
)
