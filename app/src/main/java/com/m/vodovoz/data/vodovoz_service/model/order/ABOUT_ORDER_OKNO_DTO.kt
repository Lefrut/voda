package com.m.vodovoz.data.vodovoz_service.model.order


import androidx.annotation.Keep
import com.squareup.moshi.Json

@Keep
data class ABOUT_ORDER_OKNO_DTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ABOUT_ORDER_ITEM_DTO>?
)