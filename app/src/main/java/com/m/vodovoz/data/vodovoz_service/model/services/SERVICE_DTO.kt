package com.m.vodovoz.data.vodovoz_service.model.services


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SERVICE_DTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "PREVIEW_PICTURE")
    val PREVIEW_PICTURE: String?
)