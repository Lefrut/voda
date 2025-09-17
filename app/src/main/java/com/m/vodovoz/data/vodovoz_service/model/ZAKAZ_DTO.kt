package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ZAKAZ_DTO(
    @Json(name = "BORDERCOLOR")
    val BORDERCOLOR: String?,
    @Json(name = "IDZAKAZ")
    val IDZAKAZ: Long?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "PRICE")
    val PRICE: String?,
    @Json(name = "ZAGALOVOK")
    val ZAGALOVOK: String?
)