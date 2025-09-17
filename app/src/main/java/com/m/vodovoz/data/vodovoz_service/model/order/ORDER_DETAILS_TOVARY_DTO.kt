package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_DETAILS_TOVARY_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TOVAR")
    val TOVAR: List<ORDER_DETAILS_TOVAR_DTO>?
)