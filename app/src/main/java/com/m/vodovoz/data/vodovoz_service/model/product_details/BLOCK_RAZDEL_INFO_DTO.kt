package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BLOCK_RAZDEL_INFO_DTO(
    @Json(name = "DATA")
    val DATA: BLOCK_RAZDEL_INFO_DATA_DTO?,
    @Json(name = "TITLE")
    val TITLE: String?
)