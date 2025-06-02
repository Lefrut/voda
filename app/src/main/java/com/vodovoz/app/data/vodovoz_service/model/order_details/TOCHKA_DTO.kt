package com.vodovoz.app.data.vodovoz_service.model.order_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TOCHKA_DTO(
    @Json(name = "Latitude")
    val Latitude: Double?,
    @Json(name = "Longitude")
    val Longitude: Double?
)