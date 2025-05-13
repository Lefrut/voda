package com.vodovoz.app.data.vodovoz_service.model.notification_settings


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class NotificationSettingsDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TELEFON")
    val TELEFON: TELEFON_DTO?,
    @Json(name = "LISTADATA")
    val LISTADATA: List<SWITCH_SECTION_DTO>?
)