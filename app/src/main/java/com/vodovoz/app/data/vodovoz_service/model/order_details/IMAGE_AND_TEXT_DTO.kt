package com.vodovoz.app.data.vodovoz_service.model.order_details


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class IMAGE_AND_TEXT_DTO(
    @Json(name = "KARTINKA")
    val KARTINKA: String?,
    @Json(name = "POLE")
    val POLE: String?
)