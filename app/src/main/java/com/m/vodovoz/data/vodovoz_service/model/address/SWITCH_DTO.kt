package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.google.gson.annotations.JsonAdapter

@Keep
data class SWITCH_DTO(
    @field:Json(name = "PROP_CODE")
    val PROP_CODE: String?,
    @field:Json(name = "ID")
    val ID: String?,
    @field:Json(name = "NAME")
    val NAME: String?,
    @field:Json(name = "TYPE")
    val TYPE: String?,
    @field:Json(name = "OBYAZATELNO")
    val OBYAZATELNO: String?,
    @field:Json(name = "VALUE")
    val VALUE: Any?,
    @field:Json(name = "ZABLOCKPOLE")
    val ZABLOCKPOLE: String?,
)