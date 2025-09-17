package com.m.vodovoz.data.vodovoz_service.model.auth


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class KNOPKA_AUTH_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXTCOLOR")
    val TEXTCOLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "BACGROUND")
    val BACGROUND: String?,
    @Json(name = "ID")
    val ID: String?
)