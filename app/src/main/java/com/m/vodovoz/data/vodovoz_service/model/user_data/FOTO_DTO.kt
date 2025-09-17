package com.m.vodovoz.data.vodovoz_service.model.user_data


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class FOTO_DTO(
    @Json(name = "IMG")
    val IMG: String?,
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?
)