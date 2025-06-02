package com.vodovoz.app.data.vodovoz_service.model.services


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class AllServicesDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPIS")
    val OPIS: List<SERVICE_DTO>?
)