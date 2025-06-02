package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class HIT_DTO(
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "TITLE")
    val TITLE: String?
)