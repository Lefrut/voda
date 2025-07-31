package com.vodovoz.app.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json


@Keep
data class IMAGE_DTO(
    @Json(name = "SRC")
    val SRC: String
)
