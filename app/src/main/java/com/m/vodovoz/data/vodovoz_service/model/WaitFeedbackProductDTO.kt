package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Keep
data class WaitFeedbackProductDTO(
    @Json(name = "ID")
    val id: Long?,
    @Json(name = "NAME")
    val name: String?,
    @Json(name = "DETAIL_PICTURE")
    val image: String?,
)
