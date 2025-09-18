package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SUPER_TOP_CATEGORY(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "NAME")
    val NAME: String?
)