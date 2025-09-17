package com.m.vodovoz.data.maps.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class FeatureMemberDTO(
    @Json(name = "GeoObject")
    val geoObject: GeoObjectDTO?
)