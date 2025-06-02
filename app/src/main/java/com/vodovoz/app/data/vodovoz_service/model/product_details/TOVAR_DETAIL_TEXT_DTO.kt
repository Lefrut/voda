package com.vodovoz.app.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class TOVAR_DETAIL_TEXT_DTO(
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "OPISANIE")
    val OPISANIE: String?,
    @Json(name = "TITLE")
    val TITLE: String?
)