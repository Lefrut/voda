package com.m.vodovoz.data.vodovoz_service.model.product_details

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BONUSCENA_DTO(
    @Json(name = "IKONKA")
    val IKONKA: String?,
    @Json(name = "BONUS")
    val BONUS: String?,
)