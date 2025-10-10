package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class SWITCH_DTO(
    @Json(name = "PROP_CODE")
    val PROP_CODE: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "TYPE")
    val TYPE: String?,
    @Json(name = "VALUE")
    val VALUE: Boolean?,
    @Json(name = "ZABLOCKPOLE")
    val ZABLOCKPOLE: String?,
)