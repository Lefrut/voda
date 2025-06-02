package com.vodovoz.app.data.vodovoz_service.model


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class CHECKBOXES_DTO(
    @Json(name = "OBYZATELEN")
    val OBYZATELEN: String?,
    @Json(name = "ZNACHWNIYA")
    val ZNACHWNIYA: List<CHECKBOX_DTO>?
)