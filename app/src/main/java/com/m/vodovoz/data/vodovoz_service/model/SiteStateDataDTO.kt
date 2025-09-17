package com.m.vodovoz.data.vodovoz_service.model


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class SiteStateDataDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "LOGO")
    val LOGO: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TELEFON")
    val TELEFON: String?,
    @Json(name = "EMAIL")
    val EMAIL: String?,
    @Json(name = "TIME")
    val TIME: String?,
    @Json(name = "KLYCH")
    val KLYCH: List<SITE_STATE_TRANSITION_DTO>?
)