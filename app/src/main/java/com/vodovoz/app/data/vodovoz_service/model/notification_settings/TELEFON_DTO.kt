package com.vodovoz.app.data.vodovoz_service.model.notification_settings


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class TELEFON_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ACTIVE")
    val ACTIVE: String?,
    @Json(name = "KLYCH")
    val KLYCH: String?
)