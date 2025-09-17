package com.m.vodovoz.data.maps.model


import com.squareup.moshi.JsonClass
import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
@JsonClass(generateAdapter = true)
data class YandexGeoResponseDTO(
    @Json(name = "response")
    val info: YandexGeoInfoDTO?
)