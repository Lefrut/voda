package com.vodovoz.app.data.vodovoz_service.model.ordering


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.vodovoz.app.data.vodovoz_service.model.user_data.POLE_DTO

@Keep
data class ORDER_KOMMENT_DTO(
    @Json(name = "KOMMENTARY")
    val KOMMENTARY: POLE_DTO?,
    @Json(name = "PREDYP")
    val PREDYP: ORDER_PREDYP_DTO?
)