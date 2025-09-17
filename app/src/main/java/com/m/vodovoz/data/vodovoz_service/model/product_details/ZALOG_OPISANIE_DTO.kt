package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ZALOG_OPISANIE_DTO(
    @Json(name = "DOPOPISANIE")
    val DOPOPISANIE: String?,
    @Json(name = "TEXT")
    val TEXT: String?
)