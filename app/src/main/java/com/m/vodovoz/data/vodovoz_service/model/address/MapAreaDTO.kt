package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class MapAreaDTO(
    @Json(name = "ID")
    val ID: Int?,
    @Json(name = "TEXT")
    val TEXT: String?,
    @Json(name = "MKAD")
    val MKAD: Boolean?,
    @Json(name = "COLOR")
    val COLOR: String?,
    @Json(name = "TOCHKA")
    val TOCHKA: List<List<Double>>?
)