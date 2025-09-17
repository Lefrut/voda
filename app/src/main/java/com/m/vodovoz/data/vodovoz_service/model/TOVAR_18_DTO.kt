package com.m.vodovoz.data.vodovoz_service.model


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class TOVAR_18_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TEXTBLUR")
    val TEXTBLUR: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_AUTH_DTO?
)