package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PODAROK_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPIS")
    val OPIS: String?,
    @Json(name = "SUMKORZINA")
    val SUMKORZINA: Int?,
    @Json(name = "MAXSYMMA")
    val MAXSYMMA: Int?,
    @Json(name = "KARTINKA")
    val KARTINKA: String?,

    @Json(name = "KNOPKA")
    val KNOPKA: PODAROK_KNOPKA_DTO?,
)