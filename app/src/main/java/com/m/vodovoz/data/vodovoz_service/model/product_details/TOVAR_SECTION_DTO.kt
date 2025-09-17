package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_DATA_DTO

@Keep
data class TOVAR_SECTION_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "REKOMEND")
    val REKOMEND: List<TOVAR_DATA_DTO?>?
)