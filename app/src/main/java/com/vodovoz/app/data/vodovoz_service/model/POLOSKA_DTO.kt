package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class POLOSKA_DTO(
    @Json(name = "BACKROUND")
    val BACKROUND: String?,
    @Json(name = "TEXTCOLOR")
    val TEXTCOLOR: String?
)