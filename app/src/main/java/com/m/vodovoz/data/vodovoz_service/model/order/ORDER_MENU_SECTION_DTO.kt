package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDER_MENU_SECTION_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_MENU_ITEM_DTO>?
)