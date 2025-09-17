package com.m.vodovoz.data.vodovoz_service.model.delivery_date


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class DATE_INTERVAL_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "BLOCK")
    val BLOCK: String?,
    @Json(name = "MONEY")
    val MONEY: String?
)