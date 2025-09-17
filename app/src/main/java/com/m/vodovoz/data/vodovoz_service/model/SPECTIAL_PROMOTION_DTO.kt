package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SPECTIAL_PROMOTION_DTO(
    @Json(name = "HARAKTERISTIK")
    val HARAKTERISTIK: VNYTRENNOST_DTO?,
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXT")
    val TEXT: String?
)