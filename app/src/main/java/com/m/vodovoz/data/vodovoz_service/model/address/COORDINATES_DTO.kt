package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class COORDINATES_DTO(
    @Json(name = "latitude")
    val latitude: Double?,
    @Json(name = "longitude")
    val longitude: Double?
)