package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CART_KNOPKA_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "ID")
    val ID: String?
)