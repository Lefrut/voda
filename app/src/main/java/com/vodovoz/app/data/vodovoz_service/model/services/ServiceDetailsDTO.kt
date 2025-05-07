package com.vodovoz.app.data.vodovoz_service.model.services


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ServiceDetailsDTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "DETAIL_TEXT")
    val DETAIL_TEXT: String?,
    @Json(name = "PREVIEW_PICTURE")
    val PREVIEW_PICTURE: String?,
    @Json(name = "TOVAR")
    val TOVAR: ServiceProductsDTO?,
    @Json(name = "KNOPKA")
    val KNOPKA: SERVICE_DETAILS_BUTTON_DTO?
)