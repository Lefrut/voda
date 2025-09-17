package com.m.vodovoz.data.maps.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
@JsonClass(generateAdapter = true)
data class AddressDTO(
    @Json(name = "country_code")
    val countryCode: String?,
    val formatted: String?,
    @Json(name = "postal_code")
    val postalCode: String?,
    @Json(name = "Components")
    val components: List<AddressComponentDTO>?
)