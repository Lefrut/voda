package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class USLOVIE_DTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "URL")
    val URL: String?
)