package com.m.vodovoz.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BLOCK_KNOPKA_DATA_DTO(
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TOVAR")
    val TOVAR: List<BLOCK_TOVAR_DTO>?
)