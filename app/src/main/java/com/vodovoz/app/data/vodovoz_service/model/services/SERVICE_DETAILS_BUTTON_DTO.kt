package com.vodovoz.app.data.vodovoz_service.model.services


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class SERVICE_DETAILS_BUTTON_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXTCOLOR")
    val TEXTCOLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "NAMEYSLYGA")
    val NAMEYSLYGA: String?
)