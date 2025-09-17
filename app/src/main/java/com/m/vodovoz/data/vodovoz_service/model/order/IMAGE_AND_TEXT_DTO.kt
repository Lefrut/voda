package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class IMAGE_AND_TEXT_DTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "POLE")
    val POLE: String?,
    @Json(name = "TEXT")
    val TEXT: String?
)