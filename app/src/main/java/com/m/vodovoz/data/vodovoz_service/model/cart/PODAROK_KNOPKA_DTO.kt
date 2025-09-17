package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PODAROK_KNOPKA_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "OKNOPODAROK")
    val OKNOPODAROK: OKNO_PODAROK_DTO?
)