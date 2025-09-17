package com.m.vodovoz.data.vodovoz_service.model.delivery_date


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO

@Keep
data class DATE_INTERVALS_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "INTERVAL")
    val INTERVAL: List<DATE_INTERVAL_DTO>?,
    @Json(name = "ERROR")
    val ERROR: VodovozPlaceholderDTO?
)