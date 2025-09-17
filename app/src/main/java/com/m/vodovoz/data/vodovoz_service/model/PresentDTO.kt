package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PresentDTO(
    @Json(name = "POLOSKA")
    val POLOSKA: POLOSKA_DTO?,
    @Json(name = "SYMMAZAKAZA")
    val SYMMAZAKAZA: Int?,
    @Json(name = "TEXT")
    val TEXT: String?
)