package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TOCHKA_DTO(
    @Json(name = "Latitude")
    val Latitude: Double?,
    @Json(name = "Longitude")
    val Longitude: Double?
)