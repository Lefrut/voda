package com.m.vodovoz.data.vodovoz_service.model.cart


import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.m.vodovoz.data.vodovoz_service.model.EXTENDED_PRICE_DTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_18_DTO

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
    @Json(name = "TOVAR18")
    val TOVAR18: TOVAR_18_DTO?

)

@Keep
data class PODAROK_PRICE_DTO(
    @Json(name = "OLD_PRICE")
    val OLD_PRICE: String?,
    @Json(name = "PRICE")
    val PRICE: String?,
)