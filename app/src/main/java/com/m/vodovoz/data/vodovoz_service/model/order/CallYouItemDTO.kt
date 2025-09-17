package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class CallYouItemDTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "VALUE")
    val VALUE: String?,
    @Json(name = "ZABLOCKPOLE")
    val ZABLOCKPOLE: String?,
)