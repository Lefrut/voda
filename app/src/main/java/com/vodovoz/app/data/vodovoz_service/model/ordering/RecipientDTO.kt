package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class RecipientDTO(
    @Json(name = "PHONE")
    val PHONE: String?,
    @Json(name = "FIO")
    val FIO: String?
)