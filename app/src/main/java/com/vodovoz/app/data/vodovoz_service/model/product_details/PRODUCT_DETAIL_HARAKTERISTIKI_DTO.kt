package com.vodovoz.app.data.vodovoz_service.model.product_details


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PRODUCT_DETAIL_HARAKTERISTIKI_DTO(
    @Json(name = "DATA")
    val DATA: List<HARAKTERISTIKI_DTO>?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "TITLE")
    val TITLE: String?
)