package com.vodovoz.app.data.vodovoz_service.model.order_history


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class FILTER_STATYS_DTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "NAME")
    val NAME: String?
)