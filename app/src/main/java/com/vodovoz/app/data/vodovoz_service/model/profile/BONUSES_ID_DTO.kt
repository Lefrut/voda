package com.vodovoz.app.data.vodovoz_service.model.profile


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class BONUSES_ID_DTO(
    @Json(name = "SSILKA")
    val SSILKA: String?,
    @Json(name = "IFRAME")
    val IFRAME: String?
)