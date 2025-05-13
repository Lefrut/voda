package com.vodovoz.app.data.vodovoz_service.model.notification_settings


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class SWITCH_SECTION_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<SWITCH_DTO?>?
)