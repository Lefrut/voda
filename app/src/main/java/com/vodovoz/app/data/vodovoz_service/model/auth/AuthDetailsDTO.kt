package com.vodovoz.app.data.vodovoz_service.model.auth


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.vodovoz.app.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class AuthDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "DATA")
    val DATA: List<POLE_DTO>?,
    @Json(name = "SOGLASHENIE")
    val SOGLASHENIE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: List<KNOPKA_AUTH_DTO>?,
)