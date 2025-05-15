package com.vodovoz.app.data.vodovoz_service.model.order_history


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDERS_HISTORY_PRODUCT_DTO(
    @Json(name = "ACTIVE")
    val ACTIVE: String?,
    @Json(name = "DETAIL_PICTURE")
    val DETAIL_PICTURE: String?,
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "IBLOCK_ID")
    val IBLOCK_ID: Int?,
    @Json(name = "QUANTITY")
    val QUANTITY: Int?,
    @Json(name = "NEW_QUANTITY")
    val NEW_QUANTITY: Int?
)