package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class KORZINA_KNOPKI_DTO(
    @Json(name = "BYTYLI")
    val BYTYLI: CART_KNOPKA_DTO?,
    @Json(name = "PROMOKOD")
    val PROMOKOD: KNOPKA_PROMOKOD_DTO?,
    @Json(name = "PODARKI")
    val PODARKI: CART_KNOPKA_DTO?
)