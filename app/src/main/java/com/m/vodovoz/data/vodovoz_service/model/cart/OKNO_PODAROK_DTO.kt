package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class OKNO_PODAROK_DTO(
    @Json(name = "POKUPKA")
    val POKUPKA: Boolean?,
    @Json(name = "PODAROK")
    val PODAROK: List<PRODUCT_PODAROK_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: PODAROK_KNOPKA_DTO?,
    @Json(name = "TEXT")
    val PODAROK_BANNER: PODAROK_DTO?
)
