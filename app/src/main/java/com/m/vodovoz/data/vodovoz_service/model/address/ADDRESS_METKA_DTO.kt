package com.m.vodovoz.data.vodovoz_service.model.address


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ADDRESS_METKA_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "IsEditable")
    val isEditable: Boolean?
)