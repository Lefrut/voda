package com.m.vodovoz.data.vodovoz_service.model.address

import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ADD_ADDRESS_LABEL_DTO(
    @param:Json(name = "NAME")
    val NAME: String?,
    @param:Json(name = "ID")
    val ID: String?,
)
