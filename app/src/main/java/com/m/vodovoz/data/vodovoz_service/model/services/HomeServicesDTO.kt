package com.m.vodovoz.data.vodovoz_service.model.services

import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_DTO
import com.squareup.moshi.Json

@Keep
data class HomeServicesDTO(
    @Json(name = "TITLE") val title: String?,
    @Json(name = "OPIS") val services: List<SERVICE_DTO>?,
    @Json(name = "KNOPKA") val button: KNOPKA_DTO?,
)
