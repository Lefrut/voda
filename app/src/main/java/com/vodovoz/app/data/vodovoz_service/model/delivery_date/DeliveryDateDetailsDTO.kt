package com.vodovoz.app.data.vodovoz_service.model.delivery_date


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.auth.KNOPKA_AUTH_DTO

@Keep
data class DeliveryDateDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "DATE")
    val DATE: List<DELIVERY_DATE_DTO>?,
    @Json(name = "INTERVAL")
    val INTERVAL: List<DATE_INTERVALS_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: KNOPKA_AUTH_DTO?
)