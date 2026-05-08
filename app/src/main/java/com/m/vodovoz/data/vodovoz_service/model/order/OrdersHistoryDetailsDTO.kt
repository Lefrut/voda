package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.BannerDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozButtonDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.squareup.moshi.Json

@Keep
data class OrdersHistoryDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "FILTERSTATYS")
    val FILTERSTATYS: List<FILTER_STATYS_DTO>?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDERS_HISTORY_ITEM_DTO>?,
    @Json(name = "TAB")
    val TAB: List<ORDERS_HISTORY_TAB_DTO>?,
    @Json(name = "TABS")
    val TABS: List<ORDERS_HISTORY_TAB_DTO>?,
    @Json(name = "ACTIVE_TAB")
    val ACTIVE_TAB: String?,
    @Json(name = "BANNER")
    val BANNER: List<BannerDTO>?,
    @Json(name = "ZAGALOVOK")
    val ZAGALOVOK: String?,
    @Json(name = "MESSAGE")
    val MESSAGE: String?,
    @Json(name = "IMAGE")
    val IMAGE: String?,
    @Json(name = "KNOPKA")
    val KNOPKA: VodovozButtonDTO?,
)

@Keep
data class ORDERS_HISTORY_TAB_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "ID")
    val ID: String?,
    @Json(name = "GODA")
    val GODA: List<String>?,
    @Json(name = "AVAILABLE_YEARS")
    val AVAILABLE_YEARS: List<ORDERS_HISTORY_YEAR_DTO>?,
    @Json(name = "SELECTED_YEAR")
    val SELECTED_YEAR: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDERS_HISTORY_ITEM_DTO>?,
    @Json(name = "ORDERS")
    val ORDERS: List<ORDERS_HISTORY_ITEM_DTO>?,
    @Json(name = "DATA")
    val DATA: VodovozPlaceholderDTO?,
    @Json(name = "PAGINATION")
    val PAGINATION: ORDERS_HISTORY_PAGINATION_DTO?,
    @Json(name = "ERROR")
    val ERROR: VodovozPlaceholderDTO?,
)

@Keep
data class ORDERS_HISTORY_YEAR_DTO(
    @Json(name = "YEAR")
    val YEAR: String?,
    @Json(name = "ACTIVE")
    val ACTIVE: Boolean?,
)

@Keep
data class ORDERS_HISTORY_PAGINATION_DTO(
    @Json(name = "totalPages")
    val totalPages: Int?,
    @Json(name = "currentPage")
    val currentPage: Int?,
    @Json(name = "totalCount")
    val totalCount: Int?,
    @Json(name = "limit")
    val limit: Int?,
)
