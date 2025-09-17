package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SOGLASHENIE_DTO(
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "ZAGOLOVOKi")
    val ZAGOLOVOKi: List<String>?,
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: List<String>?
)