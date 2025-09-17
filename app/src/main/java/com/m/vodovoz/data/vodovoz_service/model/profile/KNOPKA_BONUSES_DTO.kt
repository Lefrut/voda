package com.m.vodovoz.data.vodovoz_service.model.profile


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class KNOPKA_BONUSES_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "TEXTCOLOR")
    val TEXTCOLOR: String?,
    @Json(name = "ID")
    val ID: BONUSES_ID_DTO?
)