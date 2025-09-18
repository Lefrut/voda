package com.m.vodovoz.data.vodovoz_service.model.user_data


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class POLE_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "PROP_CODE")
    val PROP_CODE: String?,
    @Json(name = "OBYZATELNO")
    val OBYZATELNO: String?,
    @Json(name = "OBAZATELEN")
    val OBAZATELEN: String?,
    @Json(name = "ZABLOCKPOLE")
    val ZABLOCKPOLE: String?,
    @Json(name = "POLE")
    val POLE: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "TEXTOPIS")
    val TEXTOPIS: String?,
    @Json(name = "SPISOK")
    val SPISOK: List<SPISOK>?,
    @Json(name = "OPIS")
    val OPIS: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TEXT_V_POLE")
    val TEXT_V_POLE: String?,
    @Json(name = "TEXTVPOLE")
    val TEXTVPOLE: String?
)