package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class NALICHIE_MORE_DTO(
    @Json(name = "CVET")
    val CVET: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?
)