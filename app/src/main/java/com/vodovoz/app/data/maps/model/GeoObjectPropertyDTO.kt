package com.vodovoz.app.data.maps.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class GeoObjectPropertyDTO(
    @Json(name = "GeocoderMetaData")
    val geocoderMetaData: GeocoderMetaDataDTO?
)