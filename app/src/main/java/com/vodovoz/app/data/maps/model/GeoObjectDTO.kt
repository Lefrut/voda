package com.vodovoz.app.data.maps.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class GeoObjectDTO(
    val metaDataProperty: GeoObjectPropertyDTO?,
    val name: String?,
    val description: String?,
    val uri: String?,
    @Json(name = "Point")
    val point: PointDTO?
)