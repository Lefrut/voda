package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDER_PREDYP_ITEM_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "CODE")
    val CODE: String?,
    @Json(name = "VALUE")
    val VALUE: String?
)