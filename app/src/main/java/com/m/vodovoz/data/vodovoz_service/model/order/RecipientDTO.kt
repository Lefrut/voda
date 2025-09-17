package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class RecipientDTO(
    @Json(name = "PHONE")
    val PHONE: String?,
    @Json(name = "FIO")
    val FIO: String?
)