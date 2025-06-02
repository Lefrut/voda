package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CHATJIVO_DTO(
    @Json(name = "ACTIVE")
    val ACTIVE: String?,
    @Json(name = "SSILKA")
    val SSILKA: String?
)