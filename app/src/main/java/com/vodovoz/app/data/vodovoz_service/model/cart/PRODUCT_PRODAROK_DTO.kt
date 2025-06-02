package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class PRODUCT_PRODAROK_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "IBLOCK_ID")
    val IBLOCK_ID: Int?,
    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?
)