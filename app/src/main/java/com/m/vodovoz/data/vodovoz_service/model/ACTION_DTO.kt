package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ACTION_DTO(
    @Json(name = "ACTION")
    val ACTION: String?,
    @Json(name = "ID")
    val ID: String?
)