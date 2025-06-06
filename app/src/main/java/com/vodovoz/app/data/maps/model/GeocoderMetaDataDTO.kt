package com.vodovoz.app.data.maps.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class GeocoderMetaDataDTO(
    val precision: String?,
    val text: String?,
    val kind: String?,
    @Json(name = "Address")
    val address: AddressDTO?,
)