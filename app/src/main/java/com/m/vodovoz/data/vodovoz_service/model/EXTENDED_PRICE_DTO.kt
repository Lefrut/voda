package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class EXTENDED_PRICE_DTO(
    @Json(name = "OLD_PRICE")
    val OLD_PRICE: Int?,
    @Json(name = "PRICE")
    val PRICE: Int?,
    @Json(name = "QUANTITY_FROM")
    val QUANTITY_FROM: Int?,
    @Json(name = "QUANTITY_TO")
    val QUANTITY_TO: Int?
)