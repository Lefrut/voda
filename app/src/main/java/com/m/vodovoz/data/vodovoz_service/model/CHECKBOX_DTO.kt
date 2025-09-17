package com.m.vodovoz.data.vodovoz_service.model


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class CHECKBOX_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "TYPE")
    val TYPE: String?,
    @Json(name = "OBYAZATELNO")
    val OBYAZATELNO: String?,
    @Json(name = "ZAGOLOVOKi")
    val ZAGOLOVOKi: List<String>?,
    @Json(name = "VALUE")
    val VALUE: Any?,
    @Json(name = "ID")
    val ID: String?
)