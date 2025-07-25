package com.vodovoz.app.data.vodovoz_service.model.auth


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class AUTH_CHECKBOX_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TYPE")
    val TYPE: String?,
    @Json(name = "OBYAZATELNO")
    val OBYAZATELNO: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "ID")
    val ID: String?
)