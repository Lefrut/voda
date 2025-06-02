package com.vodovoz.app.data.vodovoz_service.model.profile


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TEXT_KNOPKA_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TEXTCOLOR")
    val TEXTCOLOR: String?
)