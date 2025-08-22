package com.vodovoz.app.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDER_POLYSHATEL_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_POLYSHATEL_ITEM_DTO>?
)