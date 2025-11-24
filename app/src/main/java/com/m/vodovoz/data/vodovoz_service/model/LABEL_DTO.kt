package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class LABEL_DTO(
    @Json(name = "NAME")
    val name: String?,
    @Json(name = "BACKGROUNDOPACITY")
    val backgroundAlpha: Float?,
    @Json(name = "BACKGROUND")
    val backgroundColor: String?,
    @Json(name = "COLOR")
    val textColor: String?
)
