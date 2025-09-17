package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CATEGORY_RAZDEL(
    @Json(name = "COUNT")
    val COUNT: Int?,
    @Json(name = "DATA")
    val DATA: List<CATEGORY_WITH_PRODUCTS_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_INT_DTO?,
    @Json(name = "NAMERAZDEL")
    val NAMERAZDEL: String?
)