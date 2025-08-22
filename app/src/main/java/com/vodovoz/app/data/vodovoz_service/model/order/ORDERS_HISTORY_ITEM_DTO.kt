package com.vodovoz.app.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json
import java.time.LocalDateTime

@Keep
data class ORDERS_HISTORY_ITEM_DTO(
    @Json(name = "ID")
    val ID: Long?,
    @Json(name = "STATUS_ID")
    val STATUS_ID: String?,
    @Json(name = "DATE_INSERT")
    val DATE_INSERT: LocalDateTime?,
    @Json(name = "PRICE")
    val PRICE: String?,
    @Json(name = "PAYED")
    val PAYED: String?,
    @Json(name = "CANCELED")
    val CANCELED: String?,
    @Json(name = "CURRENCY")
    val CURRENCY: String?,
    @Json(name = "ADDRESS")
    val ADDRESS: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "STATUS")
    val STATUS: ORDER_STATUS_DTO?,
    @Json(name = "KNOPKA")
    val KNOPKA: ORDERS_HISTORY_KNOPKA_DTO?,
    @Json(name = "ITEMS")
    val ITEMS: List<ORDERS_HISTORY_PRODUCT_DTO>?
)