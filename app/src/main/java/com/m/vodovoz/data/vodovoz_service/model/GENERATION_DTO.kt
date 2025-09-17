package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class GENERATION_DTO(
    @Json(name = "TIME")
    val TIME: String?,
    @Json(name = "TRAKING")
    val TRAKING: String?
)