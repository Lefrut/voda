package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_DETAILS_TITLE_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "OPIS")
    val OPIS: String?
)