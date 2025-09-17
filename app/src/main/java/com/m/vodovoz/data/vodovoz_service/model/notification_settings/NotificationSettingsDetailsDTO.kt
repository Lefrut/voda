package com.m.vodovoz.data.vodovoz_service.model.notification_settings


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_ORDER_DTO

@Keep
data class NotificationSettingsDetailsDTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<NOTIFICATION_SECTION_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_ORDER_DTO?
)