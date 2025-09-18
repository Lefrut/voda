package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SUPER_TOP_RAZDEL(
    @Json(name = "DATA")
    val DATA: List<SUPER_TOP_CATEGORY>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_INT_DTO?,
    @Json(name = "NAMERAZDEL")
    val NAMERAZDEL: String?
)