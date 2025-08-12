package com.vodovoz.app.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.vodovoz.app.data.vodovoz_service.model.EXTENDED_PRICE_DTO

@Keep
data class PRODUCT_PODAROK_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "IBLOCK_ID")
    val IBLOCK_ID: Int?,
    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?,
    @Json(name = "EXTENDED_PRICE")
    val EXTENDED_PRICE: PODAROK_PRICE_DTO?,
)

@Keep
data class PODAROK_PRICE_DTO(
    @Json(name = "OLD_PRICE")
    val OLD_PRICE: String?,
    @Json(name = "PRICE")
    val PRICE: String?,
)