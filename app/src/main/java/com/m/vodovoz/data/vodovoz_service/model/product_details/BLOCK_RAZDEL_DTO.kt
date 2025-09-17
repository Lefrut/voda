package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BLOCK_RAZDEL_DTO(
    @Json(name = "BRAND")
    val BRAND: BLOCK_RAZDEL_INFO_DTO?,
    @Json(name = "RAZDEL")
    val RAZDEL: BLOCK_RAZDEL_INFO_DTO?
)