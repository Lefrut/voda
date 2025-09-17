package com.m.vodovoz.data.vodovoz_service.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class FieldsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "POLYA")
    val POLYA: List<POLE_DTO>?,
)
