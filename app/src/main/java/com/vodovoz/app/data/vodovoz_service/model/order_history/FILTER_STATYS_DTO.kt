package com.vodovoz.app.data.vodovoz_service.model.order_history


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class FILTER_STATYS_DTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "NAME")
    val NAME: String?
)