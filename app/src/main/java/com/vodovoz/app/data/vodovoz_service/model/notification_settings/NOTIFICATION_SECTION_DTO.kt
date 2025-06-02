package com.vodovoz.app.data.vodovoz_service.model.notification_settings


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class NOTIFICATION_SECTION_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "POLA")
    val POLA: List<NOTIFICATION_SECTION_ITEM_DTO>?
)