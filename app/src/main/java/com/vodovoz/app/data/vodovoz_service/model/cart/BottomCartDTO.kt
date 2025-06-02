package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BottomCartDTO(
    @Json(name = "ALLSUMA")
    val ALLSUMA: Int?,
    @Json(name = "QUANTITY")
    val QUANTITY: Int?,
    @Json(name = "TOVAROV")
    val TOVAROV: Int?
)