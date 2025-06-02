package com.vodovoz.app.data.vodovoz_service.model.notification_settings


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class NOTIFICATION_SECTION_ITEM_DTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "MESSAGE")
    val MESSAGE: String?,
    @Json(name = "KLYCH")
    val KLYCH: String?,
    @Json(name = "FIELD_TYPE")
    val FIELD_TYPE: String?,
    @Json(name = "ZAPRETREDAK")
    val ZAPRETREDAK: String?,
    @Json(name = "VALUE")
    val VALUE: String?
)