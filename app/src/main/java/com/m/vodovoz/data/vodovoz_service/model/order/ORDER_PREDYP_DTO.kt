package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class ORDER_PREDYP_DTO(
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<ORDER_PREDYP_ITEM_DTO>?,
    @Json(name = "POLE")
    val POLE: POLE_DTO?
)