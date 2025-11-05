package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.COLORFUL_KNOPKA_DTO

@Keep
data class ADDRESS_METKA_OKNO_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TEXT_V_POLE")
    val TEXT_V_POLE: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: COLORFUL_KNOPKA_DTO?
)