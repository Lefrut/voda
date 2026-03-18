package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CartDetailsDTO(
    @Json(name = "PODAROK")
    val PODAROK: PODAROK_DTO?,
    @Json(name = "PODAROK_OFORMLENIE")
    val PODAROK_OFORMLENIE: OKNO_PODAROK_DTO?,
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "COUNT")
    val COUNT: String?,
    @Json(name = "KORZINA")
    val KORZINA: List<KORZINA_PRODUCT_DTO>?,
    @Json(name = "KNOPKI")
    val KNOPKI: KORZINA_KNOPKI_DTO?,
    @Json(name = "ITOG")
    val ITOG: List<ITOG_ITEM_DTO>?
)
