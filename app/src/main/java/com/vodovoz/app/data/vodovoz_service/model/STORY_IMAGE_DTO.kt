package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class STORY_IMAGE_DTO(
    @Json(name = "IMAGE")
    val IMAGE: String?
)