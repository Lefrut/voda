package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class AddressLabelsDTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "METKI")
    val METKI: List<ADDRESS_METKA_DTO>?,
    @Json(name = "OKNO")
    val OKNO: ADDRESS_METKA_OKNO_DTO?
)