package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ITOG_ITEM_DTO(
    @Json(name = "NAME")
    val name: String?,
    @Json(name = "VALUE")
    val value: String?,
    @Json(name = "ID")
    val id: String?,
    @Json(name = "VIDNO")
    val displayValue: String?,
    @Json(name = "COLOR")
    val color: String?,
)