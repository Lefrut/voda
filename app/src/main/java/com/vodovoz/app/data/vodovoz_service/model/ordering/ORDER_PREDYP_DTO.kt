package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import androidx.annotation.Keep

@Keep
data class ORDER_PREDYP_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_PREDYP_ITEM_DTO>?
)