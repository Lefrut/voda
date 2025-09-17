package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class MapZonesDTO(
    @Json(name = "KNOPKA")
    val KNOPKA: MAP_BUTTON_DTO?,
    @Json(name = "ZONE")
    val ZONE: List<MapAreaDTO>?,
)