package com.vodovoz.app.data.vodovoz_service.model.services


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.vodovoz.app.data.vodovoz_service.model.TOVAR_DATA_DTO

@Keep
data class ServiceProductsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TOVARY")
    val TOVARY: List<TOVAR_DATA_DTO>?,
    @Json(name = "KOEFFICIENT")
    val KOEFFICIENT: Int?,
    @Json(name = "DOPTOVAR")
    val DOPTOVAR: String?
)