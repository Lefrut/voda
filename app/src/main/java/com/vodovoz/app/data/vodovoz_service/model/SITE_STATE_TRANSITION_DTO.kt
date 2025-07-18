package com.vodovoz.app.data.vodovoz_service.model


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class SITE_STATE_TRANSITION_DTO(
    @Json(name = "IMAGES")
    val IMAGES: String?,
    @Json(name = "URL")
    val URL: String?,
    @Json(name = "TYPE")
    val TYPE: String?
)