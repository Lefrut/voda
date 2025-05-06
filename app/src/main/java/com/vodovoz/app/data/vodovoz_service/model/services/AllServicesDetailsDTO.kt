package com.vodovoz.app.data.vodovoz_service.model.services


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class AllServicesDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPIS")
    val OPIS: List<SERVICE_DTO>?
)