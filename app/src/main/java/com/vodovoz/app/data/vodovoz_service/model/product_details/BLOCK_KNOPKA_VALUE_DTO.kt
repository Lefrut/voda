package com.vodovoz.app.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BLOCK_KNOPKA_VALUE_DTO(
    @Json(name = "BACKGROUND")
    val BACKGROUND: String?,
    @Json(name = "COLORTEXT")
    val COLORTEXT: String?,
    @Json(name = "TITLE")
    val TITLE: String?
)