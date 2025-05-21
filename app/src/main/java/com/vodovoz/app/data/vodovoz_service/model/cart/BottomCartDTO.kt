package com.vodovoz.app.data.vodovoz_service.model.cart


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class BottomCartDTO(
    @Json(name = "ALLSUMA")
    val ALLSUMA: Int?,
    @Json(name = "QUANTITY")
    val QUANTITY: Int?,
    @Json(name = "TOVAROV")
    val TOVAROV: Int?
)