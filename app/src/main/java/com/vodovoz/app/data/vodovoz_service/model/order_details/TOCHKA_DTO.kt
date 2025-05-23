package com.vodovoz.app.data.vodovoz_service.model.order_details


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class TOCHKA_DTO(
    @Json(name = "Latitude")
    val Latitude: Double?,
    @Json(name = "Longitude")
    val Longitude: Double?
)