package com.vodovoz.app.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ORDER_PRODUCT_PODAROK_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "COLOR")
    val COLOR: String?
)