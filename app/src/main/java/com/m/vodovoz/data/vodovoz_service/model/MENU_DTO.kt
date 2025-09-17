package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class MENU_DTO(
    @Json(name = "BORDERCOLOR")
    val BORDERCOLOR: String?,
    @Json(name = "IDKLYCH")
    val IDKLYCH: String?,
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TITLE")
    val TITLE: String?
)