package com.m.vodovoz.data.vodovoz_service.model.cart

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class DOPTOVARY_TEXT_DTO(
    @Json(name = "ID")
    val TOVARY_ID: Long?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "ARTICLE")
    val ARTICLE: String?,
)
