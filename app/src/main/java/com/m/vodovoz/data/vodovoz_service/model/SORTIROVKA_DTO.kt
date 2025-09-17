package com.m.vodovoz.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class SORTIROVKA_DTO(
    @Json(name = "DANNIESORT")
    val DANNIESORT: List<SORT_DTO?>?,
    @Json(name = "NAMEGLAV")
    val NAMEGLAV: String?
)