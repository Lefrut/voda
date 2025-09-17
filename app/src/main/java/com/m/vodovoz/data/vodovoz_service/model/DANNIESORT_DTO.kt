package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class DANNIESORT_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "SORT")
    val SORT: String?,
    @Json(name = "ZNACHIE")
    val ZNACHIE: String?
)