package com.vodovoz.app.data.vodovoz_service.model.delivery_date


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class DELIVERY_DATE_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "VALUE")
    val VALUE: String?
)