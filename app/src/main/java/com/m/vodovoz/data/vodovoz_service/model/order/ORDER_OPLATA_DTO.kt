package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import androidx.annotation.Keep

@Keep
data class ORDER_OPLATA_DTO(
    @Json(name = "ZAGOLOVOK")
    val ZAGOLOVOK: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_OPLATA_ITEM_DTO>?
)