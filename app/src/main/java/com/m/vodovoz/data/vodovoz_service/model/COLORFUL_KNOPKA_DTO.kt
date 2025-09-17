package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class COLORFUL_KNOPKA_DTO(
    @Json(name = "COLOR_BACKGROUND")
    val COLOR_BACKGROUND: String?,
    @Json(name = "COLOR_TEXT")
    val COLOR_TEXT: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
)