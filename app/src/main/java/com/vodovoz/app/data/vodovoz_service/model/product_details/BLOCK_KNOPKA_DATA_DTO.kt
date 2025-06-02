package com.vodovoz.app.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class BLOCK_KNOPKA_DATA_DTO(
    @Json(name = "KOLLTOVAR")
    val KOLLTOVAR: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "TOVAR")
    val TOVAR: BLOCK_TOVAR_DTO?
)