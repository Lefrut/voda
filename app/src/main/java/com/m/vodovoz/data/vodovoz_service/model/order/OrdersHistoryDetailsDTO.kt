package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.BannerDTO
import com.squareup.moshi.Json

@Keep
data class OrdersHistoryDetailsDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "FILTERSTATYS")
    val FILTERSTATYS: List<FILTER_STATYS_DTO>?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDERS_HISTORY_ITEM_DTO>?,
    @Json(name = "BANNER")
    val BANNER: List<BannerDTO>?,
)