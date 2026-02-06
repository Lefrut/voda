package com.m.vodovoz.data.vodovoz_service.model.order


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class ORDER_KOMMENT_DTO(
    @Json(name = "PREDYP")
    val PREDYP: ORDER_PREDYP_DTO?
)