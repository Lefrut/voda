package com.m.vodovoz.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PROFILE_TEXT_OKNO_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TEXT")
    val TEXT: String?
)