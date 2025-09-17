package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class VNYTRENNOST_DTO(
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "ACTION")
    val ACTION: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: COLORFUL_KNOPKA_DTO?
)