package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CommentMediaDTO(
    @Json(name = "VIDEO")
    val isVideo: Boolean?,
    @Json(name = "KARTINKA")
    val data: String?
)