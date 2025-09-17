package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class DetailPageUrlIos(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "URL")
    val URL: String?
)